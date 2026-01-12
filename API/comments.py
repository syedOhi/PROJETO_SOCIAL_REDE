from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from ..database import get_db
from ..models import Comment, Post
from ..schemas import CommentCreate, CommentOut
from .. import models

router = APIRouter(prefix="/comments", tags=["Comments"])

# -----------------------------
# 🔹 Create Comment
# -----------------------------
@router.post("/", response_model=CommentOut)
def create_comment(comment: CommentCreate, db: Session = Depends(get_db)):
    # 🔍 Check if post exists
    post = db.query(Post).filter(Post.id == comment.postId).first()
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")

    # 🔍 Check user
    user = db.query(models.User).filter(
        models.User.username == comment.username
    ).first()

    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # 🔥 BAN CHECK
    if user.is_banned:
        raise HTTPException(
            status_code=403,
            detail="Banned users cannot comment"
        )

    new_comment = Comment(
        postId=comment.postId,
        username=comment.username,
        text=comment.text,
        timestamp=comment.timestamp
    )

    db.add(new_comment)

    # ✅ Increase comment count safely
    post.commentCount += 1

    db.commit()
    db.refresh(new_comment)
    return new_comment

# -----------------------------
# 🔹 Get Comments for a Post
# -----------------------------
@router.get("/{post_id}", response_model=list[CommentOut])
def get_comments(post_id: int, db: Session = Depends(get_db)):
    return db.query(Comment).filter(Comment.postId == post_id).all()


# -----------------------------
# 🔹 Delete Comment
# -----------------------------
@router.delete("/{comment_id}")
def delete_comment(comment_id: int, db: Session = Depends(get_db)):
    comment = db.query(Comment).filter(Comment.id == comment_id).first()

    if not comment:
        raise HTTPException(status_code=404, detail="Comment not found")

    # Find the post the comment belongs to
    post = db.query(Post).filter(Post.id == comment.postId).first()
    if post:
        post.commentCount -= 1

    db.delete(comment)
    db.commit()

    return {"message": "Comment deleted"}
