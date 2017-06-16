VMChatDemoCall
--------------

使用环信新版 SDK3.3.0以后版本实现完整的音视频通话功能，本次实现将所有的逻辑操作都放在了 VMCallManager 类中，方便对音视频界面最小化的管理；
此项目实现了音视频过界面的最小化，以及视频通话界面本地和远程画面的大小切换等功能

### #使用版本
- AndrodiStudio 2.3.1
- Gradle 3.3
- SDK Build Tools 25.0.3
- SDK Compile 25
- SDK mini 21
- Design 25.3.1
- [ButterKnife 8.6.0](https://github.com/JakeWharton/butterknife)
- [EventBus 3.0.0](https://github.com/greenrobot/EventBus)
- [环信 SDK 3.3.2](http://www.easemob.com/download/im)
- [自己封装的工具类库，暂时只能下载源码引用 VMLibraryManager vmtools](https://github.com/lzan13/VMLibraryManager)

>PS:这边并没有将 libs 目录上传到 github，需要大家自己去环信官网下载最新的 sdk 放在 libs 下


### #实现功能
- 通话界面最小化及恢复
- 通话悬浮窗的实现，可拖动
- 视频通话界面切换
- 视频通话的录制
- 视频通话的截图
- 横竖屏的自动切换
- 呼叫对方离线时发送推送通知

### #已知问题
- 未接通时切换到悬浮窗，当接通时无法显示画面


### 更新日志
记录最近一次的更新说明

#### 2017.6.16 更新
- 增加初始化多次的判断，防止测试时经常出现自己掉线的情况
- 修改 ui 界面为中文
- 更新 IM SDK 为最新3.3.2 实现通话 UI 大小视图切换
- 解决Video通话界面内存泄露情况


### #项目截图
![首界面](/screenshots/screenshot-main.png?raw=true "首界面")
![通话界面](/screenshots/screenshot-call.png?raw=true "通话界面")
![通话界面](/screenshots/screenshot-call-horizontal.png?raw=true "通话界面")
![悬浮窗](/screenshots/screenshot-call-float-window-1.png?raw=true "悬浮窗")
![悬浮窗](/screenshots/screenshot-call-float-window-2.png?raw=true "悬浮窗")

### #关联项目
实现有一个 TV 端的应用，可以实现和移动端进行实时通话，给大家在 TV 端使用环信 SDK 进行集成音视频通话加以参考  
【[TV 端视频通话项目](https://github.com/lzan13/VMTVCall)】