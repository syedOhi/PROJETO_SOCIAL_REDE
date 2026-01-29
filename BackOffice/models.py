 
from sqlalchemy import Column, Integer, String, Boolean
from database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(100), unique=True, index=True)
    fullName = Column(String(200))
    password = Column(String(255))

    bio = Column(String(500))
    dob = Column(String(50))

    profileImageUri = Column(String(500), nullable=True)
    profileImageResId = Column(Integer, nullable=True)

    role = Column(String(50), default="user")
    is_banned = Column(Boolean, default=False)

# keep your other minimal models if you want counts, like Post/Comment etc.
class Post(Base):
    __tablename__ = "posts"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(100), index=True)
    caption = Column(String(1000))

    imageUri = Column(String(500), nullable=True)
    imageResId = Column(Integer, nullable=True)

    likeCount = Column(Integer, default=0)
    isLiked = Column(Boolean, default=False)

    timestamp = Column(String(100))
    commentCount = Column(Integer, default=0)

from sqlalchemy import Column, Integer, String
from database import Base

class Comment(Base):
    __tablename__ = "comments"
    id = Column(Integer, primary_key=True)
    postId = Column(Integer, index=True)
    username = Column(String(100))
    text = Column(String(1000))
    timestamp = Column(String(100))

class PostLike(Base):
    __tablename__ = "post_likes"
    id = Column(Integer, primary_key=True)
    postId = Column(Integer, index=True)
    username = Column(String(100))
class Follow(Base):
    __tablename__ = "follows"
    id = Column(Integer, primary_key=True)

class Message(Base):
    __tablename__ = "messages"
    id = Column(Integer, primary_key=True)

class Notification(Base):
    __tablename__ = "notifications"
    id = Column(Integer, primary_key=True)

 

class ChatRequest(Base):
    __tablename__ = "chat_requests"
    id = Column(Integer, primary_key=True)