from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from .. import models, schemas
from ..database import get_db

router = APIRouter(
    prefix="/posts",
    tags=["Posts"]
)

# CREATE POST
@router.post("/", response_model=schemas.PostOut)
def create_post(post: schemas.PostCreate, db: Session = Depends(get_db)):

    # 🔍 Get user by username
    user = db.query(models.User).filter(
        models.User.username == post.username
    ).first()

    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # 🔥 BAN CHECK (this is the key)
    if user.is_banned:
        raise HTTPException(
            status_code=403,
            detail="Banned users cannot create posts"
        )

    new_post = models.Post(
        username=post.username,
        caption=post.caption,
        imageUri=post.imageUri,
        imageResId=post.imageResId,
        timestamp=post.timestamp,
        likeCount=0,
        isLiked=False
    )

    db.add(new_post)
    db.commit()
    db.refresh(new_post)

    return new_post

# GET ALL POSTS (Feed)
@router.get("/{username}", response_model=list[schemas.PostOut])
def get_all_posts(username: str, db: Session = Depends(get_db)):
    posts = db.query(models.Post).order_by(models.Post.timestamp.desc()).all()

    for p in posts:
        is_liked = db.query(models.PostLike).filter(
            models.PostLike.postId == p.id,
            models.PostLike.username == username
        ).first()

        p.isLiked = is_liked is not None

    return posts


# GET POSTS FROM ONE USER
@router.get("/user/{username}", response_model=list[schemas.PostOut])
def get_posts_by_user(username: str, db: Session = Depends(get_db)):
    posts = db.query(models.Post).filter(
        models.Post.username == username
    ).order_by(models.Post.timestamp.desc()).all()

    return posts
    