var ws;
var SocketCreated = false;
var isUserloggedout = false;
var groom;
var gname;
var users = [];
function lockOn(str){
    var lock = document.getElementById('skm_LockPane');
    if (lock)
        lock.className = 'LockOn';
    lock.innerHTML = str;
}

function lockOff(){
    var lock = document.getElementById('skm_LockPane');
    lock.className = 'LockOff';
}

function ToggleConnectionClicked() {
	//var ip = '127.0.0.1:8080';
	var ip = '172.28.10.148:8080';
    if (SocketCreated && (ws.readyState == 0 || ws.readyState == 1)) {
        lockOn("離開聊天室...");
        SocketCreated = false;
        isUserloggedout = true;
        var msg = JSON.stringify({'command':'leave', 'roomId':groom , 'name': gname,
            'info':'離開房間'});
        ws.send(msg);
        ws.close();
    } else if(document.getElementById("roomId").value == "") {
		alert("請输入房間號!");
    } else if(document.getElementById("username").value == "") {
        alert("請輸入名稱!");
    } else {
        lockOn("進入聊天室...");
        Log("準備連接到聊天伺服器...");
        groom = document.getElementById("roomId").value;
        gname = document.getElementById("username").value;
        try {
            if ("WebSocket" in window) {
                ws = new WebSocket(
                    //'ws://localhost:8080/webSocket/INFO={"command":"enter","name":"'+ gname + '","roomId":"' + groom + '"}');
					'ws://' + ip + '/webSocket?username=' + gname + '&roomId=' + groom);
            }
            else if("MozWebSocket" in window) {
                ws = new MozWebSocket(
				//'ws://localhost:8080/webSocket/INFO={"command":"enter","name":"'+ gname + '","roomId":"' + groom + '"}');
				'ws://' + ip + '/webSocket?username=' + gname + '&roomId=' + groom);
            }
            SocketCreated = true;
            isUserloggedout = false;
        } catch (ex) {
            Log(ex, "ERROR");
            return;
        }
        document.getElementById("ToggleConnection").innerHTML = "退出聊天室";
        ws.onopen = WSonOpen;
        ws.onmessage = WSonMessage;
        ws.onclose = WSonClose;
        ws.onerror = WSonError;
    }
};
function WSonOpen() {
    lockOff();
    Log("連接已經建立。", "OK");
    $("#SendDataContainer").show();
    var msg = JSON.stringify({'command':'enter', 'roomId':groom , 'counterpart': "all",
        'info': gname + "加入聊天室"})
    ws.send(msg);
};
function WSonMessage(event) {
    Log(event.data, "INFO");
};
function WSonClose() {
    lockOff();
    if (isUserloggedout)
        Log("您已離開了房間！", "INFO");
    document.getElementById("ToggleConnection").innerHTML = "進入聊天室";
    $("#SendDataContainer").hide();
};
function WSonError() {
    lockOff();
    Log("遠程連接中斷。", "ERROR");
};
function SendDataClicked() {
    if (document.getElementById("DataToSend").value.trim() != "") {
        var msg = JSON.stringify({'command':'message', 'roomId':groom , 'counterpart': document.getElementById("DataToSendWho").value,
            'info':document.getElementById("DataToSend").value})
        ws.send(msg);
        document.getElementById("DataToSend").value = "";
    }
};
function UsersOfRoomClicked(){
	var msg = JSON.stringify({'command':'usersOfRoom', 'roomId':groom , 'counterpart': "",
        'info': ""})
    ws.send(msg);
}	
function Log(Text, MessageType) {
    if (MessageType == "OK") Text = "<span style='color: green;'>" + Text + "</span>";
    if (MessageType == "ERROR") Text = "<span style='color: red;'>" + Text + "</span>";
    if (MessageType == "INFO") Text = "<span style='color: tomato;'>" + Text + "</span>";
    document.getElementById("LogContainer").innerHTML = document.getElementById("LogContainer").innerHTML + Text + "<br />";
    var LogContainer = document.getElementById("LogContainer");
    LogContainer.scrollTop = LogContainer.scrollHeight;
};

$('#UsersOfRoomSelect').focus(function(){
	alert('hi');
});

$(document).ready(function () {
    $("#SendDataContainer").hide();
    var WebSocketsExist = true;
    try {
        var dummy = new WebSocket("ws://localhost:8989/test");
    } catch (ex) {
        try
        {
            webSocket = new MozWebSocket("ws://localhost:8989/test");
        }
        catch(ex)
        {
            WebSocketsExist = false;
        }
    }
    if (WebSocketsExist) {
        Log("您的瀏覽器支持WebSocket. 您可以嘗試連接到聊天服務器!", "OK");
        //document.getElementById("roomId").value = "請输入房間號!";
    } else {
        Log("您的瀏覽器不支持WebSocket。請選擇其他的瀏覽器再嘗試連接服務器。", "ERROR");
        document.getElementById("ToggleConnection").disabled = true;
    }
    $("#DataToSend").keypress(function(evt){
        if (evt.keyCode == 13)
        {
            $("#SendData").click();
            evt.preventDefault();
        }
    })
});
