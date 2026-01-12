from fastapi import FastAPI
from .database import Base, engine
from .routers import users, posts, comments, likes, follows, chat, notifications, auth




Base.metadata.create_all(bind=engine)

app = FastAPI(title="Social Network API")

app.include_router(users.router)
app.include_router(posts.router)
app.include_router(comments.router)
app.include_router(likes.router)
app.include_router(follows.router)
app.include_router(chat.router)
app.include_router(notifications.router)
app.include_router(auth.router)



@app.get("/")
def root():
    
    
    
    
    return {"message": "Social Network API is running!"}
