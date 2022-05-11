# FloatTest
一：depenency
Android float library
Based on floatwindow, and fixed some problems, adding automatic hiding, drag and drop deletion, automatic adaptation, horizontal and vertical screen conversion and so on.

Step 1. Add the JitPack repository to your build file

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  Step 2. Add the dependency
  
  dependencies {
	        implementation 'com.github.howlaa:FloatTest:1.0'
	}
  
  二: use
  1.your mainfest:
  
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    
  2.in your activity:
  ```
  private fun floatBasic(){
        val iv = ImageView(this)
        iv.setImageResource(R.mipmap.ic_launcher)
        FloatWindow.with(context.applicationContext)
            .setTag(FLOAT_TAG)
            .setWidth(100)
            .setHeight(Screen.width,0.2f)
            .setView(iv)
            .setDesktopShow(true)
            .setX(0)
            .setDragResId(R.mipmap.home_screen_recording_remove)
            .setDragRemoveReplaceResId(R.mipmap.floating_painted)
            .setAutoHide(hideToTime)
            .setY(Screen.height,0.45f)
            .setViewStateListener(object : ViewStateListener {
                override fun onPositionUpdate(x: Int, y: Int) {
                }

                override fun onShow() {
                }

                override fun onHide() {
                }


                override fun onDismiss() {
                }

                override fun onMoveAnimStart() {
                }

                override fun onMoveAnimEnd() {
                }

                override fun onBackToDesktop() {
                }

                override fun onDragRemoved() {
                   
                }
            })
            .build()
        FloatWindow.get(FLOAT_TAG)?.show()
    }
  
  ```
 
