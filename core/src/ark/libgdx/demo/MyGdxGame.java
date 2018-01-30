package ark.libgdx.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.UBJsonReader;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.utils.ShaderLoader;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {

	private static String tag="libgdx";
	private static boolean touch=false;
	private PerspectiveCamera camera;
	private ModelBatch modelBatch;

	TestShader shader;
	Environment environment;

	ParticleEffect ring_effect;
	ParticleEffect random_efffect;
	ParticleSystem particleSystem;

	PostProcessor postProcessor;

	static float timer=0;
	static float slower_timer=0;

	FPSLogger logger; //to capture fps

	//torus model props
	Model model;
	final int size=36;
	ModelInstance instance[];

	@Override
	public void pause()
	{
		super.pause();
	}

	@Override
	public void resume()
	{
		postProcessor.rebind();
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}
	@Override
	public void create () {

		Gdx.input.setInputProcessor(this);

		camera = new PerspectiveCamera(67,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		camera.position.set(0f, 0f, 0f);
		camera.lookAt(0f,50f,0f);
		camera.near =0.1f;

		camera.far = 200f;
		modelBatch=new ModelBatch();

		particleSystem = ParticleSystem.get();
		PointSpriteParticleBatch pointSpriteBatch = new PointSpriteParticleBatch();
		pointSpriteBatch.setCamera(camera);
		particleSystem.add(pointSpriteBatch);

		AssetManager assets = new AssetManager();
		ParticleEffectLoader.ParticleEffectLoadParameter loadParam = new ParticleEffectLoader.ParticleEffectLoadParameter(particleSystem.getBatches());
		assets.load("random_particles.pfx", ParticleEffect.class, loadParam);

		assets.finishLoading();
		ParticleEffect original_randomEffect=assets.get("random_particles.pfx");
		// we cannot use the originalEffect, we must make a copy each time we create new particle effect
		ring_effect = original_randomEffect.copy();
		random_efffect=original_randomEffect.copy();
		random_efffect.init();
		random_efffect.start();
		ring_effect.init();
		ring_effect.start();

		random_efffect.translate(new Vector3(0,100,0));

		particleSystem.add(ring_effect);
		particleSystem.add(random_efffect);


		ShaderLoader.BasePath = "shaders/";
		postProcessor = new PostProcessor( true, true, false);
		Bloom bloom = new Bloom( (int)(Gdx.graphics.getWidth() * 0.5f), (int)(Gdx.graphics.getHeight() * 0.5f) );
		bloom.setBloomIntesity(1f);
		postProcessor.addEffect( bloom );
		environment=new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));;

		shader=new TestShader();
		shader.init();

		UBJsonReader jsonReader = new UBJsonReader();

		G3dModelLoader modelLoader = new G3dModelLoader(jsonReader);
		model = modelLoader.loadModel(Gdx.files.getFileHandle("torus.g3db", Files.FileType.Internal));
		instance=new ModelInstance[size];
		int degree[]=new int[36];
		for(int j=0;j<36;j++)
		degree[j]=20*j;
		for(int i=0;i<36;i++)

		{

			instance[i]=new ModelInstance(model);
			instance[i].transform.setTranslation(0,100+100*MathUtils.sinDeg(degree[i]),0+100*MathUtils.cosDeg(degree[i]));
			instance[i].transform.rotate(1,0,0,-1*degree[i]);
		}
		logger=new FPSLogger();
	}

	@Override
	public void render () {
		logger.log();
		Gdx.gl.glClearColor(1,1,1,1);
		Gdx.gl20.glClearColor(1,1,1,1);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);

		camera.update();
		postProcessor.capture();


		camera.position.set(camera.position.x,100+(100*MathUtils.sinDeg(timer+=0.1)), 0+(100*MathUtils.cosDeg(timer+=0.1)));
		camera.lookAt(0,100+(100*MathUtils.sinDeg(timer+5)), 0+(100*MathUtils.cosDeg(timer+5)));
		Gdx.app.log(tag,String.valueOf(camera.position.x));
		if(touch)
		{
			camera.position.x+=0.05;
			camera.rotate(camera.direction, 0.1f);
		}
		else
		{
			if(camera.position.x>0.00)
			camera.position.x-=0.05;
		}
		if(timer>360)
			timer=0;
		//instance.transform.rotate(Vector3.Z,1);

		particleSystem.update(); // technically not necessary for rendering
		particleSystem.begin();
		particleSystem.draw();
		particleSystem.end();

		//render torus model
		slower_timer+=0.01;
		shader.begin(camera, modelBatch.getRenderContext());
		shader.run_time=slower_timer;

		if(slower_timer==3.2)
			slower_timer=0;
		modelBatch.begin(camera);
		for (int i=0;i<size;i++)
		modelBatch.render(instance[i],environment,shader);
		modelBatch.end();
		postProcessor.render();
		shader.end();


		//render particle effects
		modelBatch.begin(camera);
		shader.begin(camera,modelBatch.getRenderContext());
		modelBatch.render(particleSystem,environment);
		shader.end();
		modelBatch.end();

	}


	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	@Override
	public boolean keyDown(int keycode)
	{
		return false;
	}

	@Override
	public boolean keyUp(int keycode)
	{
		return false;
	}

	@Override
	public boolean keyTyped(char character)
	{
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		touch=true;
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{	touch=false;
		return true;
	}

}