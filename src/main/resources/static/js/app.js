var ws;
var socketCreated = false;
var isUserloggedout = false;

function lockOn(str) {
    var lock = document.getElementById('skm_LockPane');
    if (lock)
        lock.className = 'LockOn';
    lock.innerHTML = str;
}

function lockOff() {
    var lock = document.getElementById('skm_LockPane');
    lock.className = 'LockOff';
}

function toggleConnectionClicked() {
    var ip = '127.0.0.1:8095';
    if (socketCreated && (ws.readyState === 0 || ws.readyState === 1)) {
        lockOn("離開聊天室...");
        socketCreated = false;
        isUserloggedout = true;
        var msg = JSON.stringify({'type': 'leave'});
        ws.send(msg);
        ws.close();
    } else if ($('#channel').val() === "") {
        alert("請输入房間號!");
    // } else if ($('#account').val() === "") {
    //     alert("請輸入名稱!");
     } else {
        lockOn("進入聊天室...");
        Log("準備連接到聊天伺服器...");
        var channel = $('#channel').val();
        var account = $('#account').val();
        try {
            ws = new WebSocket('ws://' + ip + '/product/chat/websocket-chat?channel=' + channel + '&account=' + account);
            socketCreated = true;
            isUserloggedout = false;
        } catch (ex) {
            Log(ex, "ERROR");
            return;
        }
        $('#toggleConnection').html("退出聊天室");
        ws.onopen = WSonOpen;
        ws.onmessage = WSonMessage;
        ws.onclose = WSonClose;
        ws.onerror = WSonError;
    }
}

function WSonOpen() {
    lockOff();
    Log("連接已經建立。", "OK");
    $('#sendDataContainer').show();
    var msg = JSON.stringify({'type': 'enter'});
    ws.send(msg);
}

function WSonMessage(event) {
    var result = JSON.parse(event.data);
    var type = result.type;
    switch (type) {
        case "notification":    //系統通知
            Log(result.data.message, "NOTIFICATION");
            break;
        case "warn":            //警告
            Log(result.data.message, "ERROR");
            break;
        case "countOfUsers":    //在線人數
            $('#userCount').html(result.data.message);
            break;
        case "message":         //一般訊息
            Log(result.source + ":" + result.data.message, "INFO");
            break;
        case "whisper":         //密頻
            Log(result.source + "悄悄對" + result.target.message + ":" + result.data.message, "INFO");
            break;
        case "announcement":    //公告
            $('#announcement').html(result.data.message);
            break;
        case "broadcast":       //廣告
            Log("《廣播》" + result.source + ":" + result.data.message, "NOTIFICATION");
            break;
    }
}

function WSonClose() {
    lockOff();
    if (isUserloggedout)
        Log("您已離開了房間！", "NOTIFICATION");
    $('#toggleConnection').html("進入聊天室");
    $('#sendDataContainer').hide();
}

function WSonError() {
    lockOff();
    Log("遠程連接中斷。", "ERROR");
}

function sendDataClicked() {
    if ($('#content').val().trim() !== "") {
        var messageType = $('#target').val().trim() !== "" ? 'whisper': 'message';
        var msg = JSON.stringify({
            'type': messageType,
            'target': $('#target').val(),
            'data': $('#content').val(),
        });
        ws.send(msg);
        $('#content').val("");
    }
}

function Log(Text, MessageType) {
    if (MessageType === "OK") Text = "<span style='color: green;'>" + Text + "</span>";
    if (MessageType === "NOTIFICATION") Text = "<span style='color: #82c6ff;'>" + Text + "</span>";
    if (MessageType === "ERROR") Text = "<span style='color: tomato;'>" + Text + "</span>";
    if (MessageType === "INFO") Text = "<span style='color: black;'>" + Text + "</span>";
    $('#logContainer').html($('#logContainer').html() + Text + "<br />");
    var logContainer = document.getElementById("logContainer");
    logContainer.scrollTop = logContainer.scrollHeight;
}

$(document).ready(function () {

    $('#dataToSend').keypress(function (evt) {
        if (evt.keyCode == 13) {
            $("#SendData").click();
            evt.preventDefault();
        }
    })
});
