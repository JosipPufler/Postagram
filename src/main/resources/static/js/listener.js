class Event{
    constructor(eventType, message) {
        this.eventType = eventType
        this.message = message;
    }
}

class EventBus {
    constructor() {
        this.listeners = [];
    }

    subscribe(fn) {
        this.listeners.push(fn);
    }

    publish(data) {
        this.listeners.forEach(fn => fn(data));
    }
}

console.log(getCookie())

function getCookie() {
    return document.cookie
        .split("; ")
        .find(row => row.startsWith("JWT_TOKEN" + "="))
        ?.split("=")[1];
}

function parseJwt() {
    const base64Url = getCookie().split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
        atob(base64)
            .split("")
            .map(c => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
            .join("")
    );

    return JSON.parse(jsonPayload);
}

const eventListener = new EventBus();

eventListener.subscribe(value =>
    fetch("/rest/public/event", {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + getCookie(),
            "Content-Type": "application/json"
        },
        body: JSON.stringify(value)
    })
        .then(res => {
            if (!res.ok) throw new Error("Request failed " + res.json());
            return res.json();
        })
        .then(data => console.log(data))
        .catch(err => console.error(err))
);

export function publishPostOpenEvent(postId){
    eventListener.publish(new Event("POST_OPEN", "user " + parseJwt().sub + " clicked on post with id=" + postId))
}

export function publishPostClosedEvent(postId, time){
    eventListener.publish(new Event("POST_CLOSE", "user " + parseJwt().sub + " closed post with id=" + postId + " after " + time + " seconds"))
}