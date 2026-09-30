#!/usr/bin/env node
// Original synthetic stone impacts for PixelChess; no recordings or third-party samples.
import {mkdirSync,writeFileSync} from "node:fs";
import {dirname,resolve} from "node:path";
import {fileURLToPath} from "node:url";
export function soundBytes(kind) {
  const rate=44100,duration=kind==="move"?.160:kind==="capture"?.220:.360;
  const count=Math.round(rate*duration),samples=new Float64Array(count);
  let seed=0x50495845;
  const noise=()=>{seed=(Math.imul(seed,1664525)+1013904223)>>>0;return seed/4294967296*2-1;};
  const taps=kind==="terminal"?[[0,.80,1],[.115,.65,.91]]:[[0,kind==="capture"?1.15:1,kind==="capture"?.90:1]];
  for(const [offset,gain,pitch] of taps){
    let previous=0;
    for(let i=Math.round(offset*rate);i<count;i++){
      const t=i/rate-offset;if(t<0)continue;
      const n=noise(),high=n-previous*.72;previous=n;
      const attack=1-Math.exp(-t*5200);
      const body=.30*Math.sin(2*Math.PI*420*pitch*t)*Math.exp(-t*160);
      const ring=.28*Math.sin(2*Math.PI*1320*pitch*t)*Math.exp(-t*62)
        +.16*Math.sin(2*Math.PI*2187*pitch*t+.3)*Math.exp(-t*88)
        +.10*Math.sin(2*Math.PI*3511*pitch*t+.8)*Math.exp(-t*125);
      const impact=.38*high*Math.exp(-t*420);
      samples[i]+=(body+ring+impact)*attack*gain;
    }
  }
  let peak=0;for(const x of samples)peak=Math.max(peak,Math.abs(x));
  const scale=(kind==="capture"?.55:.45)/peak;
  const bytes=new Uint8Array(44+count*2),view=new DataView(bytes.buffer);
  const tag=(offset,text)=>{for(let i=0;i<text.length;i++)bytes[offset+i]=text.charCodeAt(i);};
  tag(0,"RIFF");view.setUint32(4,bytes.length-8,true);tag(8,"WAVE");tag(12,"fmt ");
  view.setUint32(16,16,true);view.setUint16(20,1,true);view.setUint16(22,1,true);
  view.setUint32(24,rate,true);view.setUint32(28,rate*2,true);view.setUint16(32,2,true);view.setUint16(34,16,true);
  tag(36,"data");view.setUint32(40,count*2,true);
  for(let i=0;i<count;i++){
    const fade=Math.min(1,(count-1-i)/(rate*.008));
    view.setInt16(44+i*2,Math.round(Math.max(-1,Math.min(1,samples[i]*scale*fade))*32767),true);
  }
  return bytes;
}
const root=resolve(dirname(fileURLToPath(import.meta.url)),"..");
const output=resolve(root,"app/src/main/res/raw");mkdirSync(output,{recursive:true});
for(const kind of ["move","capture","terminal"]){
  const bytes=soundBytes(kind);writeFileSync(resolve(output,"stone_"+kind+".wav"),bytes);
  console.log(kind+": "+bytes.length+" bytes");
}
