# sam-helper
山姆自动下单，提供捡漏模式和抢单模式，手机端添加购物车，自动下单，去手机端支付既可。

# 注意事项
开发中，请及时更新代码。

目前只能下单普通商品，暂无法购买全球购等渠道。

主程序不要运行超过2分钟，会被风控。

抢单不易，请勿用于商业牟利。

# 使用说明
程序端：根据抓包数据，填写UserConfig文件中的变量，运行测试（AplicationTest）查看能否正确获得购物车信息。

在58分半的时候开始运行主程序（Application），切记主程序不能运行超过2分钟，会被风控。平时可以运行捡漏模式（Sentinel）。

手机端：app上添加商品至购物车，确认下单地址，下单成功后进行付款。

电脑端：微信小程序，抓包接口数据。以及运行程序。

# 抓包说明
使用抓包软件，mac下charles win下fiddler，抓取山姆小程序打开购物车触发的这个接口

![headers](https://github.com/NotwoJack/sam-helper/blob/main/image/headers.png)

# 测试环境
Mac 微信小程序 浦东 全城送 微信支付

# 更新记录
## 2022.04.21
1. 新增保供套餐自动检测下单功能（测试中）

## 2022.04.18
1. 减少哨兵模式的请求频率避免风控
2. userconfig中的lablelist为非必填项，写死

## 2022.04.17
1. 完善代码
2. 减少并发数，避免风控
3. 更新readme
4. 添加抓包说明

## 2022.04.16
1. 哨兵捡漏模式，用于日常捡漏。
2. 完善代码，添加注释
3. 更新readme

# 特别感谢
代码结构参考至 https://github.com/JannsenYang/dingdong-helper

山姆接口参考至 https://github.com/azhan1998/sam_buy

# 群组讨论
![qrcode](https://github.com/NotwoJack/sam-helper/blob/main/image/qrcode.JPG)
