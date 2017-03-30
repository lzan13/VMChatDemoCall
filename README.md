VMChatDemoCall
--------------

使用环信新版 SDK3.3.0以后版本实现完整的音视频通话功能，本次实现将所有的逻辑操作都放在了 VMCallManager 类中，方便对音视频界面最小化的管理；
此项目实现了音视频过界面的最小化，以及视频通话界面本地和远程画面的大小切换等功能

### #使用版本
- AndrodiStudio 2.3.0
- Gradle 3.3
- SDK Build Tools 25.0.2
- SDK Compile 25
- SDK mini 19
- Design 25.3.0
- [ButterKnife 8.5.1](https://github.com/JakeWharton/butterknife)
- [EventBus 3.0.0](https://github.com/greenrobot/EventBus)
- [环信 SDK 3.3.0](http://www.easemob.com/download/im)
- [自己封装的工具类库，暂时只能下载源码引用](https://github.com/lzan13/VMLibraryManager)

>PS:这边并没有将 libs 目录上传到 github，需要大家自己去环信官网下载最新的 sdk 放在 libs 下
>PS:必须使用环信SDK3.3.0以后的版本


### #实现功能
- 通话界面最小化及恢复
- 通话悬浮窗的实现，可拖动
- 视频通话界面切换
- 视频通话的录制
- 视频通话的截图
- 横竖屏的自动切换


### #关联项目
实现有一个 TV 端的应用，可以实现和移动端进行实时通话，给大家在 TV 端使用环信 SDK 进行集成音视频通话加以参考  
【[TV 端视频通话项目](https://github.com/lzan13/VMTVCall)】


### #项目截图
![首界面](/screenshots/screenshot-main.png?raw=true "首界面")
![通话界面](/screenshots/screenshot-call.png?raw=true "通话界面")
![通话界面](/screenshots/screenshot-call-horizontal.png?raw=true "通话界面")
![悬浮窗](/screenshots/screenshot-call-float-window-1.png?raw=true "悬浮窗")
![悬浮窗](/screenshots/screenshot-call-float-window-2.png?raw=true "悬浮窗")
