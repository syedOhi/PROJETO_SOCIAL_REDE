from pydantic import BaseModel
from typing import Optional
from datetime import datetime

class FollowCreate(BaseModel):
    followerUsername: str
    followedUsername: str

class FollowOut(BaseModel):
    followerUsername: str
    followedUsername: str

    class Config:
        from_attributes = True
from pydantic import BaseModel
from typing import Optional

# -------------------------
# USER SCHEMAS (UPDATED)
# -------------------------

class UserBase(BaseModel):
    username: str
    model_config = {"from_attributes": True}


class UserCreate(UserBase):
    fullName: str
    password: str


class UserUpdate(BaseModel):
    fullName: Optional[str] = None
    bio: Optional[str] = None
    dob: Optional[str] = None
    profileImageUri: Optional[str] = None
    profileImageResId: Optional[int] = None
    model_config = {"from_attributes": True}


class UserOut(UserBase):
    id: int
    fullName: str
    bio: Optional[str] = ""
    dob: Optional[str] = ""
    profileImageUri: Optional[str] = None
    profileImageResId: Optional[int] = None
    role: str
    is_banned: bool

    # ✅ removed created_at / last_active (not in your DB)
    # ✅ removed password (don’t return it)

    model_config = {"from_attributes": True}


class LoginRequest(BaseModel):
    username: str
    password: str
# -------------------------
# POST SCHEMAS
# -------------------------

class PostBase(BaseModel):
    username: str
    caption: str
    imageUri: Optional[str] = None
    imageResId: Optional[int] = None

    model_config = {"from_attributes": True}


class PostCreate(PostBase):
    timestamp: int


class PostOut(PostBase):
    id: int
    likeCount: int
    isLiked: bool
    timestamp: int
    commentCount: int

    model_config = {"from_attributes": True}


# -------------------------
# COMMENT SCHEMAS
# -------------------------

class CommentBase(BaseModel):
    postId: int
    username: str
    text: str

    model_config = {"from_attributes": True}


class CommentCreate(CommentBase):
    timestamp: int


class CommentOut(CommentBase):
    id: int
    timestamp: int


# -------------------------
# FOLLOW SCHEMAS
# -------------------------

class FollowBase(BaseModel):
    followerUsername: str
    followedUsername: str

    model_config = {"from_attributes": True}


class FollowOut(FollowBase):
    pass


# -------------------------
# CHAT MESSAGE SCHEMAS
# -------------------------

class ChatMessageBase(BaseModel):
    sender: str
    receiver: str
    message: str

    model_config = {"from_attributes": True}


class ChatMessageCreate(ChatMessageBase):
    timestamp: int
    isVoice: bool = False
    emoji: Optional[str] = None


class ChatMessageOut(ChatMessageCreate):
    id: int
    isRead: bool


# -------------------------
# NOTIFICATION SCHEMAS
# -------------------------

class NotificationBase(BaseModel):
    username: str
    message: str
    type: str
    targetUsername: str

    model_config = {"from_attributes": True}


class NotificationCreate(BaseModel):
    id: str
    username: str
    message: str
    timestamp: int
    seen: bool = False
    type: str
    targetUsername: str



class NotificationOut(NotificationCreate):
    seen: bool
