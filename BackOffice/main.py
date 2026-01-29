from fastapi import FastAPI, Request, Form, Depends
from fastapi.responses import HTMLResponse, RedirectResponse
from starlette.templating import Jinja2Templates
from starlette.middleware.sessions import SessionMiddleware
from fastapi import Depends
from database import get_db
import models
from sqlalchemy import text
from sqlalchemy import or_
app = FastAPI(title="Backoffice - Social Network")
templates = Jinja2Templates(directory="templates")

# Session cookie (change later)
app.add_middleware(SessionMiddleware, secret_key="CHANGE_THIS_SECRET_KEY")

ADMIN_USER = "admin"
ADMIN_PASS = "admin123"


def require_admin(request: Request):
    if request.session.get("is_admin") is True:
        return True
    return RedirectResponse(url="/login", status_code=302)


@app.get("/", response_class=HTMLResponse)
def home():
    return RedirectResponse(url="/login", status_code=302)


@app.get("/login", response_class=HTMLResponse)
def login_page(request: Request):
    return templates.TemplateResponse("login.html", {"request": request, "error": None})


@app.post("/login", response_class=HTMLResponse)
def login_submit(request: Request, username: str = Form(...), password: str = Form(...)):
    if username == ADMIN_USER and password == ADMIN_PASS:
        request.session["is_admin"] = True
        return RedirectResponse(url="/dashboard", status_code=302)

    return templates.TemplateResponse("login.html", {"request": request, "error": "Wrong username or password"})


@app.get("/logout")
def logout(request: Request):
    request.session.clear()
    return RedirectResponse(url="/login", status_code=302)

from sqlalchemy import text


@app.get("/dashboard", response_class=HTMLResponse)
def dashboard(request: Request, db=Depends(get_db)):
    ok = require_admin(request)
    if ok is not True:
        return ok


    def table_count(table_name: str) -> int:
        return db.execute(text(f"SELECT COUNT(*) FROM {table_name}")).scalar() or 0


    stats = {
        "users": table_count("users"),
        "posts": table_count("posts"),
        "comments": table_count("comments"),
        "follows": table_count("follows"),
        "messages": table_count("messages"),
        "likes": table_count("post_likes"),
        "notifications": table_count("notifications"),
        "chat_requests": table_count("chat_requests"),
    }


    return templates.TemplateResponse(
        "dashboard.html",
        {"request": request, "stats": stats}
    )

@app.get("/posts", response_class=HTMLResponse)
def posts_page(request: Request, q: str = "", db=Depends(get_db)):
    ok = require_admin(request)
    if ok is not True:
        return ok


    query = db.query(models.Post)


    if q:
        like = f"%{q}%"
        query = query.filter(
            or_(
                models.Post.caption.ilike(like),
                models.Post.username.ilike(like)
            )
        )


    posts = query.order_by(models.Post.id.desc()).all()


    return templates.TemplateResponse(
        "posts.html",
        {"request": request, "posts": posts, "q": q}
    )

@app.get("/posts/{post_id}", response_class=HTMLResponse)
def post_details_page(post_id: int, request: Request, db=Depends(get_db)):
    ok = require_admin(request)
    if ok is not True:
        return ok

    p = db.query(models.Post).filter(models.Post.id == post_id).first()
    if not p:
        return HTMLResponse("<h3>Post not found</h3><p><a href='/posts'>Back</a></p>", status_code=404)

    return templates.TemplateResponse(
        "post_details.html",
        {"request": request, "p": p}
    )


@app.post("/posts/{post_id}/delete")
def delete_post(post_id: int, request: Request, db=Depends(get_db)):
    ok = require_admin(request)
    if ok is not True:
        return ok

    try:
        # delete related likes + comments first (IMPORTANT: uses postId)
        db.query(models.PostLike).filter(models.PostLike.postId == post_id).delete(synchronize_session=False)
        db.query(models.Comment).filter(models.Comment.postId == post_id).delete(synchronize_session=False)

        p = db.query(models.Post).filter(models.Post.id == post_id).first()
        if p:
            db.delete(p)

        db.commit()
    except Exception as e:
        db.rollback()
        return HTMLResponse(
            f"<h3>Delete failed</h3><pre>{str(e)}</pre><p><a href='/posts'>Back</a></p>",
            status_code=500
        )

    return RedirectResponse(url="/posts", status_code=302)

@app.get("/comments", response_class=HTMLResponse)
def comments(request: Request):
    ok = require_admin(request)
    if ok is not True:
        return ok

    return HTMLResponse("""
        <h2>💬 Comments (placeholder)</h2>
        <p><a href="/dashboard">Back</a> | <a href="/logout">Logout</a></p>
        <p>Next: we will show comments from database here.</p>
    """)


@app.get("/users", response_class=HTMLResponse)
def users_page(request: Request, q: str = "", db=Depends(get_db)):
    ok = require_admin(request)
    if ok is not True:
        return ok

    query = db.query(models.User)

    if q:
        like = f"%{q}%"
        query = query.filter(
            or_(
                models.User.username.ilike(like),
                models.User.fullName.ilike(like)
            )
        )

    users = query.order_by(models.User.id.desc()).all()

    return templates.TemplateResponse(
        "users.html",
        {"request": request, "users": users, "q": q}
    )


@app.get("/users/{user_id}", response_class=HTMLResponse)
def user_details_page(user_id: int, request: Request, db=Depends(get_db)):
    ok = require_admin(request)
    if ok is not True:
        return ok

    u = db.query(models.User).filter(models.User.id == user_id).first()
    if not u:
        return HTMLResponse("<h3>User not found</h3><p><a href='/users'>Back</a></p>", status_code=404)

    return templates.TemplateResponse(
        "user_details.html",
        {"request": request, "u": u}
    )


@app.post("/users/{user_id}/toggle-ban")
def toggle_ban_user(user_id: int, request: Request, db=Depends(get_db)):
    ok = require_admin(request)
    if ok is not True:
        return ok

    u = db.query(models.User).filter(models.User.id == user_id).first()
    if u:
        u.is_banned = not bool(u.is_banned)
        db.commit()

    return RedirectResponse(url="/users", status_code=302)


@app.post("/users/{user_id}/delete")
def delete_user(user_id: int, request: Request, db=Depends(get_db)):
    ok = require_admin(request)
    if ok is not True:
        return ok

    u = db.query(models.User).filter(models.User.id == user_id).first()
    if u:
        db.delete(u)
        db.commit()

    return RedirectResponse(url="/users", status_code=302)


@app.get("/analytics", response_class=HTMLResponse)
def analytics_page(request: Request, db=Depends(get_db)):
    ok = require_admin(request)
    if ok is not True:
        return ok

    def table_count(table_name: str) -> int:
        return db.execute(text(f"SELECT COUNT(*) FROM {table_name}")).scalar() or 0

    stats = {
        "users": table_count("users"),
        "posts": table_count("posts"),
        "comments": table_count("comments"),
        "likes": table_count("post_likes"),
    }

    # Trending posts (most likes, then comments)
    trending_posts = (
        db.query(models.Post)
        .order_by(models.Post.likeCount.desc(), models.Post.commentCount.desc())
        .limit(10)
        .all()
    )

    # Trending hashtags (basic extraction from caption)
    import re
    tag_counts = {}
    all_posts = db.query(models.Post.caption).all()

    for (caption,) in all_posts:
        if not caption:
            continue
        tags = re.findall(r"#\w+", caption)
        for t in tags:
            t = t.lower()
            tag_counts[t] = tag_counts.get(t, 0) + 1

    trending_hashtags = [
        {"tag": k, "count": v}
        for k, v in sorted(tag_counts.items(), key=lambda x: x[1], reverse=True)[:10]
    ]

    return templates.TemplateResponse(
        "analytics.html",
        {
            "request": request,
            "stats": stats,
            "trending_posts": trending_posts,
            "trending_hashtags": trending_hashtags
        }
    )