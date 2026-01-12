from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from .. import models, schemas
from ..database import get_db

router = APIRouter(
    prefix="/chat",
    tags=["Chat"]
)

# SEND MESSAGE
@router.post("/send", response_model=schemas.ChatMessageOut)
def send_message(msg: schemas.ChatMessageCreate, db: Session = Depends(get_db)):

    new_msg = models.ChatMessage(
        sender=msg.sender,
        receiver=msg.receiver,
        message=msg.message,
        timestamp=msg.timestamp,
        isVoice=msg.isVoice,
        emoji=msg.emoji,
        isRead=False
    )

    db.add(new_msg)
    db.commit()
    db.refresh(new_msg)

    return new_msg


# GET CONVERSATION BETWEEN TWO USERS
@router.get("/conversation")
def get_conversation(user1: str, user2: str, db: Session = Depends(get_db)):

    msgs = db.query(models.ChatMessage).filter(
        (
            (models.ChatMessage.sender == user1) &
            (models.ChatMessage.receiver == user2)
        ) |
        (
            (models.ChatMessage.sender == user2) &
            (models.ChatMessage.receiver == user1)
        )
    ).order_by(models.ChatMessage.timestamp.asc()).all()

    return [
        {
            "id": m.id,
            "sender": m.sender,
            "receiver": m.receiver,
            "message": m.message,
            "timestamp": m.timestamp,
            "isVoice": m.isVoice,
            "emoji": m.emoji,
            "isRead": m.isRead
        }
        for m in msgs
    ]


# LIST USERS YOU HAVE CHATS WITH
@router.get("/conversations/{username}")
def get_conversations(username: str, db: Session = Depends(get_db)):

    msgs = db.query(models.ChatMessage).filter(
        (models.ChatMessage.sender == username) |
        (models.ChatMessage.receiver == username)
    ).all()

    users = set()

    for m in msgs:
        if m.sender != username:
            users.add(m.sender)
        if m.receiver != username:
            users.add(m.receiver)

    return list(users)


# GET UNREAD MESSAGE COUNT FOR A CONVERSATION
@router.get("/unread")
def unread_count(sender: str, receiver: str, db: Session = Depends(get_db)):

    count = db.query(models.ChatMessage).filter(
        models.ChatMessage.sender == sender,
        models.ChatMessage.receiver == receiver,
        models.ChatMessage.isRead == False
    ).count()

    return {"unread": count}


# MARK ALL MESSAGES AS READ
@router.post("/mark-read")
def mark_read(sender: str, receiver: str, db: Session = Depends(get_db)):

    msgs = db.query(models.ChatMessage).filter(
        models.ChatMessage.sender == sender,
        models.ChatMessage.receiver == receiver,
        models.ChatMessage.isRead == False
    ).all()

    for msg in msgs:
        msg.isRead = True

    db.commit()

    return {"message": "Marked as read"}


# UPDATE EMOJI REACTION
@router.post("/react")
def react_to_message(messageId: int, emoji: str, db: Session = Depends(get_db)):

    msg = db.query(models.ChatMessage).filter(models.ChatMessage.id == messageId).first()

    if not msg:
        raise HTTPException(status_code=404, detail="Message not found")

    msg.emoji = emoji
    db.commit()
    db.refresh(msg)

    return {"message": "Emoji updated", "id": msg.id, "emoji": msg.emoji}

@router.post("/send")
def send_message(msg: schemas.ChatMessageCreate, db: Session = Depends(get_db)):

    # Check if receiver follows sender
    follows_back = db.query(models.Follow).filter(
        models.Follow.followerUsername == msg.receiver,
        models.Follow.followedUsername == msg.sender
    ).first()

    # If NOT following back → create request
    if not follows_back:
        existing_request = db.query(models.ChatRequest).filter(
            models.ChatRequest.sender == msg.sender,
            models.ChatRequest.receiver == msg.receiver
        ).first()

        if not existing_request:
            req = models.ChatRequest(
                sender=msg.sender,
                receiver=msg.receiver,
                timestamp=msg.timestamp
            )
            db.add(req)
            db.commit()

        return {
            "status": "request_pending",
            "message": "Chat request sent"
        }

    # Else → normal message
    new_msg = models.ChatMessage(
        sender=msg.sender,
        receiver=msg.receiver,
        message=msg.message,
        timestamp=msg.timestamp,
        isVoice=msg.isVoice,
        emoji=msg.emoji,
        isRead=False
    )

    db.add(new_msg)
    db.commit()
    db.refresh(new_msg)

    return new_msg


    # check if receiver follows sender
    follows_back = db.query(models.Follow).filter(
        models.Follow.followerUsername == msg.receiver,
        models.Follow.followedUsername == msg.sender
    ).first()

    if not follows_back:
        # create chat request if not exists
        existing = db.query(models.ChatRequest).filter(
            models.ChatRequest.sender == msg.sender,
            models.ChatRequest.receiver == msg.receiver
        ).first()

        if not existing:
            req = models.ChatRequest(
                sender=msg.sender,
                receiver=msg.receiver,
                timestamp=msg.timestamp
            )
            db.add(req)
            db.commit()

        return {"status": "request_pending"}

    # else → save message normally
    message = models.ChatMessage(
        sender=msg.sender,
        receiver=msg.receiver,
        message=msg.message,
        timestamp=msg.timestamp
    )
    db.add(message)
    db.commit()
    db.refresh(message)

    return message
@router.get("/requests/{username}")
def get_chat_requests(username: str, db: Session = Depends(get_db)):

    return db.query(models.ChatRequest).filter(
        models.ChatRequest.receiver == username,
        models.ChatRequest.accepted == False
    ).all()

@router.post("/requests/accept")
def accept_chat_request(sender: str, receiver: str, db: Session = Depends(get_db)):

    req = db.query(models.ChatRequest).filter(
        models.ChatRequest.sender == sender,
        models.ChatRequest.receiver == receiver
    ).first()

    if not req:
        raise HTTPException(status_code=404, detail="Request not found")

    req.accepted = True
    db.commit()

    return {"message": "Chat request accepted"}


    req = db.query(models.ChatRequest).filter(
        models.ChatRequest.sender == sender,
        models.ChatRequest.receiver == receiver
    ).first()

    if not req:
        raise HTTPException(status_code=404, detail="Request not found")

    req.accepted = True
    db.commit()

    return {"message": "Chat request accepted"}
@router.delete("/requests")
def delete_chat_request(sender: str, receiver: str, db: Session = Depends(get_db)):

    req = db.query(models.ChatRequest).filter(
        models.ChatRequest.sender == sender,
        models.ChatRequest.receiver == receiver
    ).first()

    if req:
        db.delete(req)
        db.commit()

    return {"message": "Chat request removed"}
