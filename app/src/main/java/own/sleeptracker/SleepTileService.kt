package own.sleeptracker

import android.util.Log
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.wear.tiles.*
import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DimensionBuilders.dp
import androidx.wear.tiles.DimensionBuilders.sp
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement
import androidx.wear.tiles.ModifiersBuilders.*
import androidx.wear.tiles.material.ButtonColors
import androidx.wear.tiles.material.Colors
import com.google.android.horologist.tiles.ExperimentalHorologistTilesApi
import com.google.android.horologist.tiles.SuspendingTileService
import java.util.*


private const val RESOURCES_VERSION = "0"
val theme = Colors(
    /*primary=*/ 0xFFD0BCFF.toInt(), /*onPrimary=*/ 0xFF381E72.toInt(),
    /*surface=*/ 0xFF202124.toInt(), /*onSurface=*/ 0xFFFFFFFF.toInt()
)
val buttonColors = ButtonColors.secondaryButtonColors(theme)

@OptIn(ExperimentalHorologistTilesApi::class)
class SleepTileService : SuspendingTileService() {

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .build()
    }

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val singleTileTimeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(tileLayout()).build()

                    )
                    .build()
            )
            .build()

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            ///.setFreshnessIntervalMillis((30).toLong()) //Battery Eater!!!
            .setTimeline(singleTileTimeline)
            .build()
    }


    private fun tileLayout(): LayoutElement {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val seconds = preferences.getInt("time", 0)
        val gethours: Int = seconds / 3600
        val getminutes: Int = seconds % 3600 / 60
        val getsecs: Int = seconds % 60
        val gettime: String = java.lang.String.format(
            Locale.getDefault(),
            "%d:%02d:%02d",
            gethours,
            getminutes,
            getsecs
        )
        val text = gettime
        Log.i("owntrackerwatch", "tile updated")
        return LayoutElementBuilders.Box.Builder()
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.expand())
            .setModifiers(
                Modifiers.Builder()
                    .setBackground(
                        Background.Builder()
                            .setColor(argb(ContextCompat.getColor(this, R.color.black)))
                            .build()
                    ).build()
            )

            .addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(text).setFontStyle(
                        LayoutElementBuilders.FontStyle.Builder().setSize(sp(43f))
                            .setColor(argb(ContextCompat.getColor(this, R.color.aqua))).build()
                    ).build()
            ).setModifiers(
                Modifiers.Builder()
                    .setPadding(
                        Padding.Builder()
                            //.setStart(dp(0f))
                            //.setEnd(dp(0f))
                            .setTop(dp(-10f))
                            //.setBottom(dp(3f))
                            .build()
                    )
                    .setClickable(
                        Clickable.Builder()
                            .setId("update")
                            .setOnClick(ActionBuilders.LoadAction.Builder().build())
                            .build()
                    ).build()

            )

            .addContent(startButton()).build()

    }


    private fun startButton() =
        LayoutElementBuilders.Text.Builder()
            .setText("Start").setFontStyle(
                LayoutElementBuilders.FontStyle.Builder().setSize(sp(19f))
                    .setColor(argb(ContextCompat.getColor(this, R.color.lime))).build()
            ).setModifiers(
                Modifiers.Builder()
                    .setPadding(
                        Padding.Builder()
                            //.setStart(dp(0f))
                            //.setEnd(dp(0f))
                            .setTop(dp(113f))
                            //.setBottom(dp(3f))
                            .build()
                    )
                    // TODO: Add click (START)
                    .setClickable(
                        Clickable.Builder()
                            .setId("start")
                            .setOnClick(
                                ActionBuilders.LaunchAction.Builder()
                                .setAndroidActivity(
                                    ActionBuilders.AndroidActivity.Builder()
                                    .setClassName(MainActivity::class.java.getName())
                                    .setPackageName(this.packageName)
                                   .build()
                               ).build()
                            ).build()


                    )
                    // TODO: Add click (END)
                    .build()
            )
            .build()





}


