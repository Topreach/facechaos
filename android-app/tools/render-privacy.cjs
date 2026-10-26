const fs = require('node:fs');
const path = require('node:path');
const {pathToFileURL} = require('node:url');
const {spawn} = require('node:child_process');
const delay = ms => new Promise(resolve => setTimeout(resolve, ms));
(async () => {
  const root = path.resolve(__dirname, '../..');
  const profile = path.join(root, '_publish/privacy-cdp-render');
  fs.mkdirSync(profile, {recursive:true});
  const chrome = spawn('C:/Program Files/Google/Chrome/Application/chrome.exe', [
    '--headless=new', '--disable-gpu', '--no-first-run', '--remote-debugging-port=0',
    '--user-data-dir=' + profile, 'about:blank'
  ], {windowsHide:true, stdio:'ignore'});
  let socket;
  try {
    const portFile = path.join(profile, 'DevToolsActivePort');
    for(let i=0;!fs.existsSync(portFile)&&i<100;i++) await delay(100);
    const port = fs.readFileSync(portFile, 'utf8').split('\n')[0];
    const targets = await (await fetch(`http://127.0.0.1:${port}/json/list`)).json();
    socket = new WebSocket(targets.find(target=>target.type==='page').webSocketDebuggerUrl);
    await new Promise((resolve,reject)=>{socket.onopen=resolve;socket.onerror=reject;});
    let id=0;const pending=new Map();
    socket.onmessage=event=>{const message=JSON.parse(event.data);if(pending.has(message.id)){const {resolve,reject}=pending.get(message.id);pending.delete(message.id);message.error?reject(new Error(message.error.message)):resolve(message.result);}};
    const send=(method,params={})=>new Promise((resolve,reject)=>{pending.set(++id,{resolve,reject});socket.send(JSON.stringify({id,method,params}));});
    await send('Page.enable');
    for(const [name,width,height] of [['mobile',412,1000],['desktop',1280,1100]]) {
      await send('Emulation.setDeviceMetricsOverride',{width,height,deviceScaleFactor:1,mobile:name==='mobile'});
      await send('Page.navigate',{url:pathToFileURL(path.join(root,'web/privacy/privacy.html')).href});
      await delay(500);
      const check=await send('Runtime.evaluate',{expression:'({viewport:innerWidth,scroll:document.documentElement.scrollWidth})',returnByValue:true});
      if(check.result.value.scroll>width) throw new Error(name+' layout overflows');
      const image=await send('Page.captureScreenshot',{format:'png',captureBeyondViewport:false});
      fs.writeFileSync(path.join(root,'dist/privacy-'+name+'.png'),Buffer.from(image.data,'base64'));
      console.log('PASS: '+name+' '+width+'px viewport; no horizontal overflow');
    }
  } finally {if(socket)socket.close();chrome.kill();}
})().catch(error=>{console.error(error.message);process.exitCode=1;});
