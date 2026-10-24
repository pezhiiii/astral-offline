#!/usr/bin/env python3
"""Fetch only the explicitly pinned upstream engine; never executed by app."""
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
PIN="6b9ba4f2e8c1436511956f0b3c309e2c9e642071"
p=ROOT/"vendor/llama.cpp"
if not p.exists():
 p.mkdir(parents=True)
 subprocess.run(["git","init",str(p)],check=True)
 subprocess.run(["git","-C",str(p),"remote","add","origin","https://github.com/ggml-org/llama.cpp.git"],check=True)
subprocess.run(["git","-C",str(p),"fetch","--depth","1","origin",PIN],check=True)
subprocess.run(["git","-C",str(p),"checkout","--detach",PIN],check=True)
