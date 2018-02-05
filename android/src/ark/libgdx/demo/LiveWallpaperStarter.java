package ark.libgdx.demo;

/**
 * Created by JAYAN on 31-01-2018.
 */
import com.badlogic.gdx.Game;
public class LiveWallpaperStarter extends Game{

    @Override
    public void create() {
        setScreen(new LiveWallpaperScreen(this));
    }

}