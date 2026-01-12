from sqlalchemy import Column, Integer, String, Boolean, ForeignKey, BigInteger
from sqlalchemy.orm import relationship
from .database import Base


# -------------------------
# USER TABLE
# -------------------------
class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, index=True, nullable=False)
    fullName = Column(String(100), nullable=False)
    password = Column(String(255), nullable=False)
    bio = Column(String(255), default="")
    dob = Column(String(50), default="")
    profileImageUri = Column(String(255), nullable=True)
    profileImageResId = Column(Integer, nullable=True)
    role = Column(String(20), default="user")          # if not already
    is_banned = Column(Boolean, default=False)     
    posts = relationship("Post", back_populates="user")


# -------------------------
# POSTS TABLE
# -------------------------
class Post(Base):
    __tablename__ = "posts"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), ForeignKey("users.username"), index=True, nullable=False)
    caption = Column(String(255), nullable=False)
    imageUri = Column(String(255), nullable=True)
    imageResId = Column(Integer, nullable=True)
    likeCount = Column(Integer, default=0)
    isLiked = Column(Boolean, default=False)
    timestamp = Column(BigInteger)
    commentCount = Column(Integer, default=0)  # ✅ NEW
    user = relationship("User", back_populates="posts")

# -------------------------
# POST LIKES TABLE
# -------------------------
class PostLike(Base):
    __tablename__ = "post_likes"

    id = Column(Integer, primary_key=True, index=True)
    postId = Column(Integer, ForeignKey("posts.id"), nullable=False)
    username = Column(String(50), ForeignKey("users.username"), nullable=False)

# -------------------------
# COMMENTS TABLE
# -------------------------
class Comment(Base):
    __tablename__ = "comments"

    id = Column(Integer, primary_key=True, index=True)
    postId = Column(Integer, ForeignKey("posts.id"), index=True, nullable=False)
    username = Column(String(50), ForeignKey("users.username"), nullable=False)
    text = Column(String(255), nullable=False)
    timestamp = Column(BigInteger)


# -------------------------
# FOLLOW TABLE
# -------------------------
class Follow(Base):
    __tablename__ = "follows"

    followerUsername = Column(String(50), ForeignKey("users.username"), primary_key=True)
    followedUsername = Column(String(50), ForeignKey("users.username"), primary_key=True)


# -------------------------
# CHAT MESSAGES TABLE
# -------------------------
class ChatMessage(Base):
    __tablename__ = "messages"

    id = Column(Integer, primary_key=True, index=True)
    sender = Column(String(50), ForeignKey("users.username"), index=True)
    receiver = Column(String(50), ForeignKey("users.username"), index=True)
    message = Column(String(500), nullable=False)
    timestamp = Column(BigInteger)
    isVoice = Column(Boolean, default=False)
    emoji = Column(String(10), nullable=True)
    isRead = Column(Boolean, default=False)

    


# -------------------------
# NOTIFICATIONS TABLE
# -------------------------
class Notification(Base):
    __tablename__ = "notifications"

    id = Column(String(100), primary_key=True)  # UUID as string
    username = Column(String(50), ForeignKey("users.username"), nullable=False)
    message = Column(String(255), nullable=False)
    timestamp = Column(BigInteger)
    seen = Column(Boolean, default=False)
    type = Column(String(50), nullable=False)
    targetUsername = Column(String(50), ForeignKey("users.username"), index=True)


class ChatRequest(Base):
    __tablename__ = "chat_requests"

    id = Column(Integer, primary_key=True, index=True)
    sender = Column(String(50), ForeignKey("users.username"), nullable=False)
    receiver = Column(String(50), ForeignKey("users.username"), nullable=False)
    accepted = Column(Boolean, default=False)
    timestamp = Column(BigInteger)
