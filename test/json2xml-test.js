var jsonxml = require('jsontoxml');

var xml = jsonxml({
    root:{
        text:'Some date example',
        date:function(){
            return (new Date())+'';
        }
    }
})

console.log(xml);
