import "./style.css";
import { CameraPreview } from '../../dist/esm/index.js'

const app = document.querySelector<HTMLDivElement>("#app")!;

setTimeout(() => {
  app.querySelector<HTMLButtonElement>("#startCamera")!.addEventListener("click", async () => {
    try {
      await CameraPreview.start({
        parent: 'cameraPreview',
        position: 'rear',
        toBack: true,
        className: 'camera-preview'
      });
    } catch (e) {
      console.error(e);
    }
  });
  app.querySelector<HTMLButtonElement>("#cameraCapture")!.addEventListener("click", async () => { 
    try {
      const result = await CameraPreview.capture({});
      console.log(result);
    } catch (e) {
      console.error(e);
    }
  });
  app.querySelector<HTMLButtonElement>("#flipCamera")!.addEventListener("click", async () => {
    try {
      await CameraPreview.flip();
    } catch (e) {
      console.error(e);
    }
  });
  app.querySelector<HTMLButtonElement>("#stopCamera")!.addEventListener("click", async () => {
    try {
      await CameraPreview.stop();
    } catch (e) {
      console.error(e);
    }
  });
}, 1000)

app.innerHTML = `
  <h1>Hello Vite!</h1>
  <a href="https://vitejs.dev/guide/features.html" target="_blank">Documentation</a>
  <div id="cameraPreview"></div>
  <button id="cameraCapture">Capture image</button>
  <button id="startCamera">Start Camera</button>
  <button id="stopCamera">Stop Camera</button>
  <button id="flipCamera">Flip Camera</button>
`;
