from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from ..database import get_db
from .. import models
from ..schemas import FollowCreate, FollowOut

router = APIRouter(prefix="/follows", tags=["Follows"])

# ------------------------------
# FOLLOW
# ------------------------------
@router.post("/", response_model=FollowOut)
def follow_user(body: FollowCreate, db: Session = Depends(get_db)):

    follower = body.followerUsername
    followed = body.followedUsername

    if follower == followed:
        raise HTTPException(status_code=400, detail="Cannot follow yourself")

    exists = db.query(models.Follow).filter(
        models.Follow.followerUsername == follower,
        models.Follow.followedUsername == followed
    ).first()

    if exists:
        raise HTTPException(status_code=400, detail="Already following")

    follow = models.Follow(
        followerUsername=follower,
        followedUsername=followed
    )
    db.add(follow)
    db.commit()
    db.refresh(follow)

    return follow


# ------------------------------
# UNFOLLOW
# ------------------------------
@router.delete("/", response_model=FollowOut)
def unfollow_user(body: FollowCreate, db: Session = Depends(get_db)):

    follower = body.followerUsername
    followed = body.followedUsername

    follow = db.query(models.Follow).filter(
        models.Follow.followerUsername == follower,
        models.Follow.followedUsername == followed
    ).first()

    if not follow:
        raise HTTPException(status_code=404, detail="Relationship not found")

    db.delete(follow)
    db.commit()

    return follow


# ------------------------------
# FOLLOWER COUNT
# ------------------------------
@router.get("/followerCount/{username}")
def follower_count(username: str, db: Session = Depends(get_db)):
    return db.query(models.Follow).filter(
        models.Follow.followedUsername == username
    ).count()


# ------------------------------
# FOLLOWING COUNT
# ------------------------------
@router.get("/followingCount/{username}")
def following_count(username: str, db: Session = Depends(get_db)):
    return db.query(models.Follow).filter(
        models.Follow.followerUsername == username
    ).count()


# ------------------------------
# IS FOLLOWING?
# ------------------------------
@router.get("/isFollowing/{follower}/{followed}")
def is_following(follower: str, followed: str, db: Session = Depends(get_db)):
    follow = db.query(models.Follow).filter(
        models.Follow.followerUsername == follower,
        models.Follow.followedUsername == followed
    ).first()

    return follow is not None


# ------------------------------
# LIST USERS I AM FOLLOWING
# ------------------------------
@router.get("/following/{username}")
def get_following_users(username: str, db: Session = Depends(get_db)):

    results = db.query(models.Follow).filter(
        models.Follow.followerUsername == username
    ).all()

    return [f.followedUsername for f in results]
