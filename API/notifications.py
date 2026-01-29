from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from .. import models, schemas
from ..database import get_db

router = APIRouter(
    prefix="/notifications",
    tags=["Notifications"]
)

# CREATE NOTIFICATION
@router.post("/", response_model=schemas.NotificationOut)
def create_notification(notification: schemas.NotificationCreate, db: Session = Depends(get_db)):

    new_notif = models.Notification(
        id=notification.id,
        username=notification.username,
        message=notification.message,
        timestamp=notification.timestamp,
        seen=False,
        type=notification.type,
        targetUsername=notification.targetUsername
    )

    db.add(new_notif)
    db.commit()
    db.refresh(new_notif)

    return new_notif


# GET ALL NOTIFICATIONS FOR A USER
@router.get("/{username}", response_model=list[schemas.NotificationOut])
def get_notifications(username: str, db: Session = Depends(get_db)):

    notifs = db.query(models.Notification).filter(
        models.Notification.targetUsername == username
    ).order_by(models.Notification.timestamp.desc()).all()

    return notifs


# MARK NOTIFICATION AS SEEN
@router.post("/seen/{notif_id}")
def mark_as_seen(notif_id: str, db: Session = Depends(get_db)):

    notif = db.query(models.Notification).filter(
        models.Notification.id == notif_id
    ).first()

    if not notif:
        raise HTTPException(status_code=404, detail="Notification not found")

    notif.seen = True
    db.commit()

    return {"message": "Notification marked as seen"}


# DELETE NOTIFICATION
@router.delete("/{notif_id}")
def delete_notification(notif_id: str, db: Session = Depends(get_db)):

    notif = db.query(models.Notification).filter(
        models.Notification.id == notif_id
    ).first()

    if not notif:
        raise HTTPException(status_code=404, detail="Notification not found")

    db.delete(notif)
    db.commit()

    return {"message": "Notification deleted"}
