from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import models, schemas
from ..database import get_db

router = APIRouter(
    prefix="/auth",
    tags=["Auth"]
)

from sqlalchemy.sql import func   # add this at the top of the file

@router.post("/login", response_model=schemas.UserOut)
def login_user(data: schemas.UserCreate, db: Session = Depends(get_db)):
    user = db.query(models.User).filter(
        models.User.username == data.username
    ).first()

    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    if user.password != data.password:
        raise HTTPException(status_code=401, detail="Invalid password")

    # 🔥 THIS IS THE INTEGRATION WITH BACKOFFICE
    if user.is_banned:
        raise HTTPException(
            status_code=403,
            detail="User is banned by admin"
        )

    # ✅ UPDATE LAST ACTIVE HERE
    user.last_active = func.now()
    db.commit()
    db.refresh(user)

    return user