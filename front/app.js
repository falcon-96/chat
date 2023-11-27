var uname = prompt('Enter your name:');
//$('#name').val = uname;
document.getElementById('name').setAttribute('value', uname);
document.addEventListener('submit', clearInput);

window.onload = connect;
document.getElementById('message').addEventListener("focus", () => {
    sendTyping(true);
}, false);
document.getElementById('message').addEventListener('keydown', () => {
    sendTyping(true);
}, false);
document.getElementById('message').addEventListener("blur", () => {
    sendTyping(false);
}, false);
document.getElementById('msg-form').addEventListener('submit', () => {
    // document.getElementById('message').blur;
    sendTyping(false);
}, false);

document.getElementById('connection-switch').addEventListener('click', () => {
    var switch_btn = document.getElementById('connection-switch');
    if (switch_btn.checked) {
        connect();
    } else {
        disconnect();
    }
});
const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/chat'
});

stompClient.onConnect = (frame) => {
    $('#status-result').empty().append('Online');
    console.log("in");
    setConnected(true);
    sendConnectedUser();
    stompClient.subscribe('/topic/users', (users) => {
        var obj = JSON.parse(users.body);
        showConnectedUser(obj);
    });

    stompClient.subscribe('/topic/greetings', (greeting) => {
        showGreeting(JSON.parse(greeting.body));
    });

    stompClient.subscribe('/topic/extras', (extra) => {
        var obj = JSON.parse(extra.body);
        if (obj.action == 'typing') {
            showTyping(obj);
        } else if (obj.action == 'not-typing') {
            exitTyping();
        } else {
            showNotification(obj);
        }

    });
};
let c = 1;
function showConnectedUser(obj) {
    $('#user-area-ul li').remove();
    for (let i = 0; i < obj.length; i++) {
        var user = obj[i];
        $('#user-area-ul').append('<li class="list-group-item" id=' + user + '>' + user + '</li>');
    }

}
stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
    $('#status-result').empty().append('Offline');
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
        // initConnect;
    }
    else {
        $("#conversation").hide();
    }
    // $("#greetings").html("");
}

function connect() {
    stompClient.activate();
    document.getElementById('connection-switch').checked = true;
    window.onbeforeunload = disconnect;
}


function sendConnectedUser() {
    body1 = {
        'username': uname,
        'connected': 1,
        'messageType': 'user',
        'original': uname
    };
    stompClient.publish({
        destination: "/app/users",
        body: JSON.stringify(body1)
    });

}

function disconnect() {
    document.getElementById('connection-switch').checked = false;
    $('#status-result').empty().append('Offline');
    document.getElementById('user-area-ul').innerHTML = '';
    body1 = {
        'username': uname,
        'connected': 0,
        'messageType': 'user',
        'original': uname
    };
    stompClient.publish({
        destination: "/app/users",
        body: JSON.stringify(body1)
    });


    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}


function showGreeting(message) {
    var dt = new Date(message.time);

    if (dt.getDate() == new Date().getDate()) {
        dt = 'Today ' + dt.getHours() + ':' + dt.getMinutes();
    } else {
        dt = dt.toDateString() + ' ' + dt.getHours + ':' + dt.getMinutes;
    }
    if (message.from == uname) {
        $("#boot-card").append('<p id="chat-text-own" style="text-align: right"><span class="author">You</span><br><span class="ind-chat-box" id="my-msg"><span id="message-area">' + message.text + '</span><br><span>' + dt + "</span></span></p><br>");
    }
    else {
        $("#boot-card")
            .append('<p id="chat-text-others"><span class="ind-chat-box" id="others-msg"><span id="message-area">'
                + message.text
                + '</span><br><span>'
                + dt
                + '</span></span><span class="author">'
                + message.from + '</span></p><br>');
    }

    var chatBox = document.getElementById("boot-card");
    chatBox.scrollTop = chatBox.scrollHeight - chatBox.clientHeight;

}

function showNotification(notification) {
    $('#notification').empty().append('<p>' + notification.result + '</p>');
    $('#notification').fadeOut(2000, ()=>{
        $('#notification').empty();
        $('#notification').show();
    });
}

function sendName() {
    document.getElementById('message').blur();
    var inputName = $('#name').val();
    var message = $("#message").val();

    if (message == null || message == '') {
        return;
    }

    body1 = {
        'from': $("#name").val(),
        'text': message,
        'time': ' ',
        'messageType': 'message'
    };

    stompClient.publish({
        destination: "/app/hello",
        body: JSON.stringify(body1)
    });

    if (inputName != uname) {
        user = {
            'username': inputName,
            'connected': 1,
            'messageType': 'user',
            'original': uname
        };
        stompClient.publish({
            destination: "/app/users",
            body: JSON.stringify(user)
        });
        uname = inputName;
    }
}

function sendTyping(status) {
    extra = {};
    if (status == true) {
        extra = {
            'name': uname,
            'action': 'typing',
            'result': ''
        }
    }
    else {
        extra = {
            'name': uname,
            'action': 'not-typing',
            'result': ''
        }
    }


    stompClient.publish({
        destination: "/app/extra",
        body: JSON.stringify(extra)
    })
}

function showTyping(extra) {
    exitTyping();
    if (document.getElementById('typing') == null) {

        $("#boot-card").append('<h6 id="typing">' + extra.result + '</h6>');
    }

}

function exitTyping() {
    $("#boot-card #typing").remove();
}

function clearInput() {
    document.getElementById('message').value = '';
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $("#connect").click(() => connect());
    $("#disconnect").click(() => disconnect());
    $("#send").click(() => sendName());
});