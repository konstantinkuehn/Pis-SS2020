    var http = new XMLHttpRequest();

    function sendRequestGET(path = '', query = '') {
        http.open('GET', path + '?' + query);
        http.send();
    }

    http.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            document.getElementById('board').innerHTML = this.responseText;
        }
    }

    function sendMove(field) {
        sendRequestGET('move', 'pos=' + field);
    }

    function loadInnerTable() {
        sendRequestGET('rows');
    }

      window.onload = setupWebSocket;
      function renderBoard(){

    sendRequestGET('render')
      }
    function setupWebSocket() {



    const ws = new WebSocket(`ws://localhost:7070/`);
           ws.onopen = event => {
            console.log('connection established');
        }
     ws.onmessage = messageEvent => {
     var id = messageEvent.data.substring(0,1)
     var mes = messageEvent.data.substring(1)
            if(id ==0)
            {
          renderBoard()
            } else if(id ==1)
            {
            document.getElementById('timeFeedback').innerHTML = mes;
            }else if (id=2){
            document.getElementById('interaction').innerHTML = mes;

            }

        }
    }
