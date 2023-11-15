var uname = prompt('Enter your name:');
console.log(uname);
//$('#name').val = uname;

document.getElementById('name').setAttribute('value', uname);
document.addEventListener('submit', clearInput);
window.onload = connect;

const stompClient = new StompJs.Client({
    //brokerURL: 'ws://localhost:8080/chat'
    brokerURL: 'ws://localhost:8080/chat'
});

stompClient.onConnect = (frame) => {
    console.log("in");
    setConnected(true);
    sendConnectedUser();
    stompClient.subscribe('/topic/users', (users) => {
        console.log("topic users");
        var obj = JSON.parse(users.body);
        showConnectedUser(obj);
    });

    stompClient.subscribe('/topic/greetings', (greeting) => {
        showGreeting(JSON.parse(greeting.body));
    });
};
let c = 1;
function showConnectedUser(obj) {
    $('#user-area li').remove();
    for (let i = 0; i < obj.length; i++) {
        var user = obj[i];
        $('#user-area').append('<li id=' + user + '>' + user + '</li>');
    }

}
stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
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
    }
    else {
        $("#conversation").hide();
    }
    // $("#greetings").html("");
}

function connect() {
    stompClient.activate();

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
    $('#user-area li').remove();
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

    if (message.from == uname) {
        $("#boot-card").append('<p id="chat-text" style="text-align: right"><span class="ind-chat-box">You: ' + message.text + '<br><span>' + message.time + "</span></span></p><br>");
        //  $("#chat-area").append('<p id="chat-text" style="text-align: right"><span id="ind-chat-box">' + message.from + ": " + message.text + message.time + "</span></p>");
    }
    else {
        $("#boot-card").append('<p id="chat-text"><span class="ind-chat-box">' + message.from + ": " + message.text + '<br><span>' + message.time + "</span></span></p><br>");
        // $("#chat-area").append('<p id="chat-text"><span id="ind-chat-box">' + message.from + ": " + message.text + message.time + "</span></p>");
    }

    var chatBox = document.getElementById("boot-card");
    chatBox.scrollTop = chatBox.scrollHeight - chatBox.clientHeight;

}

function sendName() {

    var inputName = $('#name').val();

    body1 = {
        'from': $("#name").val(),
        'text': $("#message").val(),
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
        console.log(user);
        stompClient.publish({
            destination: "/app/users",
            body: JSON.stringify(user)
        });
        uname = inputName;
    }
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