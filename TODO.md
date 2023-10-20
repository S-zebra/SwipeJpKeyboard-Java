# TO-DO
- Graphics refactoring
  - Let each components have own PGraphics and paint the content
  - PApplet class collects PGraphics and draws by image()

old:
```java
keyboard.draw();
```
new:
```java
image(keyboard.draw(), keyboard.x, keyboard.y);
```

- Make key layout table into class
- 