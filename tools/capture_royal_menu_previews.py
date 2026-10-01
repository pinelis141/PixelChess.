#!/usr/bin/env python3
"""Capture real Android emulator screenshots of the fully themed menu flows."""
from pathlib import Path
import re, subprocess, time, xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'build/royal-menu-previews';OUT.mkdir(parents=True,exist_ok=True)
ADB=Path(subprocess.check_output(['which','adb'],text=True).strip())

def run(*args,timeout=45):
    return subprocess.check_output([str(ADB),*args],text=True,timeout=timeout,stderr=subprocess.STDOUT)

def wait():
    time.sleep(1.2)
    run('shell','uiautomator','dump','/sdcard/royal-menu-window.xml',timeout=30)
    raw=run('shell','cat','/sdcard/royal-menu-window.xml',timeout=10)
    return ET.fromstring(raw[raw.find('<?xml'):])

def target(fragment,attribute='text'):
    root=wait()
    for node in root.iter('node'):
        value=node.attrib.get(attribute,'')
        if fragment.lower() in value.lower() and node.attrib.get('clickable')=='true':return node
    for node in root.iter('node'):
        value=node.attrib.get(attribute,'')
        if fragment.lower() in value.lower() and node.attrib.get('bounds'):return node
    raise AssertionError('Android view not found: '+fragment)

def tap(fragment,attribute='text'):
    node=target(fragment,attribute);bounds=node.attrib['bounds']
    x1,y1,x2,y2=map(int,re.findall(r'\d+',bounds))
    run('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2));wait()

def shot(name):
    result=subprocess.run([str(ADB),'exec-out','screencap','-p'],capture_output=True,check=True,timeout=30)
    path=OUT/(name+'.png');path.write_bytes(result.stdout)
    if len(result.stdout)<100_000:raise AssertionError('Invalid/empty screenshot: '+name)
    print('Captured',path.name,len(result.stdout),'bytes',flush=True)

deadline=time.monotonic()+60
while True:
    root=wait()
    home_text=' '.join(n.attrib.get(k,'') for n in root.iter('node') for k in ('text','content-desc'))
    if all(label.lower() in home_text.lower() for label in ('PARTIDA LOCAL','JOGAR CONTRA BOT','MULTIPLAYER BLUETOOTH','SKINS DO TABULEIRO','CONFIGURAÇÕES')):break
    if time.monotonic()>=deadline:raise AssertionError('Timed out waiting for the complete home menu')
for label in ('PARTIDA LOCAL','JOGAR CONTRA BOT','MULTIPLAYER BLUETOOTH','SKINS DO TABULEIRO','CONFIGURAÇÕES'):
    if not any(label.lower() in ' '.join(n.attrib.get(k,'') for k in ('text','content-desc')).lower() for n in root.iter('node')):
        raise AssertionError('Home menu missing '+label)
shot('01-biblioteca-real')
tap('SKINS DO TABULEIRO','content-desc');shot('02-selecao-de-skins')
run('shell','input','keyevent','4');wait()
tap('CONFIGURAÇÕES','content-desc');shot('03-configuracoes')
run('shell','input','keyevent','4');wait()
tap('JOGAR CONTRA BOT','content-desc');shot('04-dificuldade-do-bot')
tap('Normal');shot('05-cor-do-jogador')
tap('Brancas');shot('06-tempo-de-partida')
run('shell','input','keyevent','4');wait()
print('Six real Android emulator menu screenshots: PASS',flush=True)
