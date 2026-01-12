from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from ..schemas import UserOut
from ..models import User

from .. import models, schemas
from ..database import get_db

router = APIRouter(
    prefix="/users",
    tags=["Users"]
)

# =============================
# CREATE USER
# =============================
@router.post("/", response_model=schemas.UserOut)
def create_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    existing = db.query(models.User).filter(
        models.User.username == user.username
    ).first()
    
    if existing:
        raise HTTPException(status_code=400, detail="Username already taken")

    new_user = models.User(
        username=user.username,
        fullName=user.fullName,
        password=user.password,
        bio="",
        dob="",
        profileImageUri=None,
        profileImageResId=None,
        role="user",          # ✅ ADD THIS
        is_banned=False       # ✅ ADD THIS
    )

    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    return new_user

# =============================
# UPDATE USER
# =============================
@router.put("/{username}", response_model=schemas.UserOut)
def update_user(username: str, data: schemas.UserUpdate, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.username == username).first()

    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # Update only provided fields
    if data.bio is not None:
        user.bio = data.bio

    if data.dob is not None:
        user.dob = data.dob

    if data.profileImageUri is not None:
        user.profileImageUri = data.profileImageUri

    if data.profileImageResId is not None:
        user.profileImageResId = data.profileImageResId

    if data.fullName is not None:
        user.fullName = data.fullName

 

    db.commit()
    db.refresh(user)
    return user


# =============================
# SEARCH USERS
# =============================
@router.get("/search", response_model=list[UserOut])
def search_users(query: str, db: Session = Depends(get_db)):
    return (
        db.query(User)
        .filter(User.username.like(f"%{query}%"))
        .all()
    )


# =============================
# GET ALL USERS
# =============================
@router.get("/", response_model=list[schemas.UserOut])
def get_users(db: Session = Depends(get_db)):
    return db.query(models.User).all()


# =============================
# GET USER BY USERNAME
# =============================
@router.get("/{username}", response_model=schemas.UserOut)
def get_user(username: str, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.username == username).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user
    
