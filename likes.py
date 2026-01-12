from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from .. import models
from ..database import get_db

router = APIRouter(
    prefix="/likes",
    tags=["Likes"]
)


@router.post("/{postId}/{username}")
def like_post(postId: int, username: str, db: Session = Depends(get_db)):

    # 🔍 Check user
    user = db.query(models.User).filter(
        models.User.username == username
    ).first()

    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # 🔥 BAN CHECK
    if user.is_banned:
        raise HTTPException(
            status_code=403,
            detail="Banned users cannot like posts"
        )

    existing = db.query(models.PostLike).filter(
        models.PostLike.postId == postId,
        models.PostLike.username == username
    ).first()

    if existing:
        raise HTTPException(status_code=400, detail="Already liked")

    new_like = models.PostLike(postId=postId, username=username)
    db.add(new_like)

    # 🔍 Update like count safely
    post = db.query(models.Post).filter(models.Post.id == postId).first()
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")

    post.likeCount += 1

    db.commit()
    return {"message": "Post liked"}

@router.delete("/{postId}/{username}")
def unlike_post(postId: int, username: str, db: Session = Depends(get_db)):
    existing = db.query(models.PostLike).filter(
        models.PostLike.postId == postId,
        models.PostLike.username == username
    ).first()

    if not existing:
        raise HTTPException(status_code=400, detail="Not liked yet")

    db.delete(existing)

    # Update like count
    post = db.query(models.Post).filter(models.Post.id == postId).first()
    post.likeCount -= 1
    if post.likeCount < 0:
        post.likeCount = 0

    db.commit()
    return {"message": "Post unliked"}

    existing = (
        db.query(models.PostLike)
        .filter(models.PostLike.postId == postId,
                models.PostLike.username == username)
        .first()
    )

    if not existing:
        raise HTTPException(status_code=400, detail="Not liked yet")

    db.delete(existing)
    db.commit()

    return {"message": "Post unliked"}
