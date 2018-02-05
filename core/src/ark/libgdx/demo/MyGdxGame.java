package ark.libgdx.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
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
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.UBJsonReader;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.filters.Blur;
import com.bitfire.utils.ShaderLoader;

public class MyGdxGame extends ApplicationAdapter  {

	private static String tag="libgdx";
	private static boolean touch=false;
	private PerspectiveCamera camera;
	CameraInputController cameraInputController;
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

	public void create_camera()
	{
		camera = new PerspectiveCamera(67,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		camera.position.set(0f, 700f, 0f);
		camera.lookAt(0f,200f,0f);
		camera.near =0.1f;
		camera.far = 300f;
		cameraInputController=new CameraInputController(camera);
		Gdx.input.setInputProcessor(cameraInputController);

	}
	public void create_particlesystem()
	{
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

		random_efffect.translate(new Vector3(0,200,0));
		random_efffect.scale(1,2.5f,2.5f);

		//particleSystem.add(ring_effect);
		particleSystem.add(random_efffect);


	}
	public void create_shader()
	{
		ShaderLoader.BasePath = "shaders/";
		postProcessor = new PostProcessor( true, true, true);
		Bloom bloom = new Bloom( (int)(Gdx.graphics.getWidth() * 0.25f), (int)(Gdx.graphics.getHeight() * 0.25f) );
		bloom.setBloomIntesity(2f);
		postProcessor.addEffect( bloom );
		environment=new Environment();
		//environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
		//environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));;
		shader=new TestShader();
		shader.init();

	}
	public void load_models()
	{
		UBJsonReader jsonReader = new UBJsonReader();
		G3dModelLoader modelLoader = new G3dModelLoader(jsonReader);
		model = modelLoader.loadModel(Gdx.files.getFileHandle("torus.g3db", Files.FileType.Internal));
		instance=new ModelInstance[size];
		int degree[]=new int[size];
		for(int j=0;j<size;j++)
			degree[j]=10*j;
		for(int i=0;i<size;i++)
		{

			instance[i]=new ModelInstance(model);
			//use scl to scale the size of the model
			//instance[i].transform.scl(MathUtils.random(1,4)/3f);
			instance[i].transform.setTranslation(0,200+200*MathUtils.sinDeg(degree[i]),0+200*MathUtils.cosDeg(degree[i]));
			instance[i].transform.rotate(1,0,0,-1*degree[i]);
		}

	}
	@Override
	public void create () {
		
		create_camera();
		modelBatch=new ModelBatch();
		create_particlesystem();
		create_shader();
		load_models();
		logger=new FPSLogger();
	}

	@Override
	public void render () {
		logger.log();
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);


		camera.update();
		postProcessor.capture();

		//camera_movement();

		camera.position.set(camera.position.x+x_offset,200+(200*MathUtils.sinDeg(timer+=0.1))+y_offset,
				0+(200*MathUtils.cosDeg(timer+=0.1))+z_offset);

		camera.lookAt(0,200+(200*MathUtils.sinDeg(timer+5)), 0+(200*MathUtils.cosDeg(timer+5)));

		if(timer>360)
			timer=0;
		//instance.transform.rotate(Vector3.Z,1);

		particleSystem.update(); // technically not necessary for rendering
		particleSystem.begin();
		particleSystem.draw();
		particleSystem.end();


		slower_timer+=0.001;
		shader.begin(camera, modelBatch.getRenderContext());
		shader.run_time=slower_timer;

		if(slower_timer==3.200)
			slower_timer=0;
		modelBatch.begin(camera);
		for (int i=0;i<size;i++)
		modelBatch.render(instance[i],environment,shader);
		modelBatch.end();
		postProcessor.render();
		shader.end();


		//render particle effects
		modelBatch.begin(camera);
		modelBatch.render(particleSystem,environment);
		modelBatch.end();

	}

	static float x_offset=0;
	static float y_offset=0;
	static float z_offset=0;

}