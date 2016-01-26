package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.MidpointDisplacementHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.TangentBinormalGenerator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sample 10 - How to create fast-rendering terrains from heightmaps, and how to
 * use texture splatting to make the terrain look good.
 */
public class Main extends SimpleApplication {

    private BulletAppState bulletAppState;
    private MrRoboto robo;
    private boolean moveBall = true;
    Geometry rock_shiny;
    AbstractHeightMap heightmap;
    int counter = 0;
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        initKeys();

        Logger.getLogger("").setLevel(Level.SEVERE);
        flyCam.setMoveSpeed(500);
        cam.setFrustumPerspective(45, 1.3f, 1f, 10000000.0f);
        cam.setLocation(new Vector3f(0,0,20));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        // Create the height map
        // size: grid size, 2^n+1
        // range: The range in which randomness will be added. A value of 1 will allow -1 to 1 value changes.
        // roughness: The factor by which the range will evolve at each iteration. 
        //            A value of 0.5f will halve the range at each iteration and is typically a good choice
        //            The fractal dimension is directly connected to the roughness.
        // normalizer: the map will have height values [0 .. normalizer]
        // waterLevel: all values below will be set to waterLevel. This is performed after normalization.
        int size = 1025;
        float range = 5.0f;
        float roughness = 0.5f;
        float normalizer = 4000.0f;
        float waterLevel = normalizer / 3f;
        heightmap = initFractalHeightMap(size, range, roughness, normalizer, waterLevel);

        /**
         * 2. create the actual terrain: Create a TerrainQuad and name it "my
         * terrain". The terrain quad is a wrapping data structure that contains
         * the actual heightmap, yet organizes it in a faster accessible form
         * (quad - tree), based on level of detail (LOD) required. As LOD step
         * scale we supply a Vector3f(1,1,1). The LOD depends on were the camera
         * is.
         */
        int patchSize = 65;
        TerrainQuad terrain = new TerrainQuad("my terrain", patchSize, size, heightmap.getHeightMap());
        TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
        terrain.addControl(control);

        // init material, height based textures
        Material mat = initMaterial(size, waterLevel, normalizer);
        terrain.setMaterial(mat);
        terrain.setLocalTranslation(0, -normalizer, 0);
        terrain.setLocalScale(16f, 1f, 16f);

        // physics for terrain    
        CollisionShape terrainShape = CollisionShapeFactory.createMeshShape((Node) terrain);
        RigidBodyControl landscape = new RigidBodyControl(terrainShape, 0);
        terrain.addControl(landscape);
        bulletAppState.getPhysicsSpace().add(terrain);

        //
        rootNode.attachChild(terrain);



        /**
         * Illuminated bumpy rock with shiny effect. Uses Texture from
         * jme3-test-data library! Needs light source!
         */
        Cylinder rock = new Cylinder(32, 32, 1f, 10000f);
        rock_shiny = new Geometry("Shiny rock", rock);
        //rock.setTextureMode(Cylinder.TextureMode.Projected); // better quality on spheres
        TangentBinormalGenerator.generate(rock);   // for lighting effect
        Material mat_shiny = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat_shiny.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
        mat_shiny.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
        mat_shiny.setColor("Diffuse", ColorRGBA.White); // needed for shininess
        rock_shiny.setMaterial(mat_shiny);
        rock_shiny.rotate(90f, 0f, 0f);
        rootNode.attachChild(rock_shiny);
        RigidBodyControl rbc = new RigidBodyControl(3f);
        rock_shiny.addControl(rbc);
        bulletAppState.getPhysicsSpace().add(rock_shiny);
        rbc.setLinearVelocity(new Vector3f(0,10,10));
          

        /**
         * A white, directional light source
         */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection((new Vector3f(-0.5f, -0.5f, -0.5f)).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
        
        robo = new MrRoboto(terrain.getHeightMap(), 0, 0);
        robo.findPathTo(0, 10);
        //robo.visualize(rootNode, assetManager);
        //robo.print();
        //terrain.adjustHeight(new Vector2f(35, 40), 1000);
        //terrain.adjustHeight(new Vector2f(0, 0), 1000);
        //float[] x = terrain.getHeightMap();
        //System.out.println(terrain.getWorldBound().distanceToEdge(Vector3f.ZERO));
    }
    @Override
    public void simpleUpdate(float tpf) {
        if(moveBall){
            int x = robo.path.get(counter).x;
            int y = robo.path.get(counter).y;
            float z = robo.heightMap[x][y];
            System.out.println(x + ", " + z + ", " + y);
            rock_shiny.setLocalTranslation(x , robo.heightMap[x][y], y);
            counter++;
            moveBall = false;
        }
    }
    // -------------------------------------------------------------------------
    public AbstractHeightMap initFractalHeightMap(int size, float range, float roughness, float normalizer, float waterLevel) {
        AbstractHeightMap heightmap = null;
        try {
            heightmap = new MidpointDisplacementHeightMap(size, range, roughness);
        } catch (Exception ex) {
        }
        // normalize
        heightmap.normalizeTerrain(normalizer);

        // flood
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                if (heightmap.getTrueHeightAtPoint(x, z) < waterLevel) {
                    heightmap.setHeightAtPoint(waterLevel, x, z);
                }
            }
        }

        heightmap.erodeTerrain();
        return (heightmap);
    }
    private void initKeys() {
        // You can map one or several inputs to one named action
        inputManager.addMapping("Move", new KeyTrigger(KeyInput.KEY_SPACE));
        // Add the names to the action listener.
        inputManager.addListener(actionListener, new String[]{"Move"});
    }
    //private AnalogListener actionListener = new AnalogListener() {
    //public void onAnalog(String name, float value, float tpf) {
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Move")) {
                moveBall = true;
            }
        }
    };
    // -------------------------------------------------------------------------
    // Material. Vertex Colors.
    private Material initMaterial(int terrainSize, float waterLevel, float normalizer) {
        // the material and its definitions can be found in:
        // jme3-libraries - jme3-terrain.jar
        // look at the j3md file to find the parameters
        Material mat = new Material(assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");
        //mat.getAdditionalRenderState().setWireframe(true);
        Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        Texture rock = assetManager.loadTexture("Textures/DirtWater.jpg");
        rock.setWrap(WrapMode.Repeat);
        Texture dirtWater = assetManager.loadTexture("Textures/Test.jpg");
        dirtWater.setWrap(WrapMode.Repeat);
        mat.setTexture("region1ColorMap", dirtWater);
        mat.setTexture("region2ColorMap", grass);
        mat.setTexture("region3ColorMap", dirt);
        mat.setTexture("region4ColorMap", rock);
        mat.setTexture("slopeColorMap", dirt);
        //
        float step = (normalizer - waterLevel) / 3f;
        mat.setVector3("region1", new Vector3f(0, waterLevel, 512f)); //startheight, endheight, scale
        mat.setVector3("region2", new Vector3f(waterLevel, waterLevel + step, 32f)); //startheight, endheight, scale
        mat.setVector3("region3", new Vector3f(waterLevel + step, waterLevel + 2 * step, 64f)); //startheight, endheight, scale
        mat.setVector3("region4", new Vector3f(waterLevel + 2 * step, normalizer, 32f)); //startheight, endheight, scale
        //
        mat.setFloat("terrainSize", terrainSize);
        mat.setFloat("slopeTileFactor", 32f);
        return (mat);
    }
}