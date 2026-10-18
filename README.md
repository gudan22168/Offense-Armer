# 氮气装甲（Offense Armer）

《魔法禁书目录》绢旗最爱的「氮气装甲」能力还原 mod，基于 **NeoForge 1.21.1**。

## 特性

- **氮气系统**：液氮瓶手动补充 + 左上角氮气条 HUD + Lv3 起自动回复
- **5 阶能力进化**（能力开发药逐级升阶）：
  - **Lv1 氮气装甲**：减伤 + 开启禁穿护甲
  - **Lv2 压缩装甲**：抵挡远程投射物
  - **Lv3 气垫**：免疫落地伤害 + 白色残影
  - **Lv4 氮气飞行**：基础飞行
  - **Lv5 觉醒**：绝对装甲·反射 + 低温领域（水结冰/减速/冰冻）+ 爆冲（超高速冲毁方块 + 马赫环）
- **炫酷渲染**：半透明白色氮气外壳（呼吸流动）+ 幽灵残影
- **跨模组联动**：兼容其他模组氮气（配置）+ Mekanism 反向充能

## 环境要求

- Minecraft **1.21.1**
- NeoForge **21.1.262+**
- Java **21**

## 操作

| 按键 | 功能 |
|---|---|
| `X` | 切换氮气装甲 |
| 双击空格 | 起飞（Lv4+） |
| `R` | 爆冲（Lv5，超高速 + 冲毁前方方块 + 马赫环） |
| `R + Shift` | 爆冲下潜（破坏脚下方块钻下去） |

调试命令（需 op 权限）：`/nitrogen level <0~5>`、`/nitrogen fill`、`/nitrogen set <数值>`、`/nitrogen toggle`

## 构建

```bash
gradle build
# 产物：build/libs/offense_armer-1.0-SNAPSHOT.jar
```

## 配置

`run/config/offense_armer-common.toml`，可调：氮气上限、耗氮/回氮速度、各阶减伤比例、飞行/爆冲速度、低温领域半径与伤害、Mekanism 联动气体与充能速率、冲撞是否掉物品等。

## 依赖

- **Mekanism（可选）**：`libs/Mekanism-1.21.1-10.7.19.85.jar` 为编译期依赖（compileOnly）。运行时若装有 Mekanism 则自动联动反向充能，未安装则跳过。

## 授权

All Rights Reserved。
