package com.svk.window;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.content.Context;
import android.opengl.Matrix;
import android.view.GestureDetector; // event related
import android.view.GestureDetector.OnDoubleTapListener; // event related
import android.view.GestureDetector.OnGestureListener; // event related
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

public class GLESView extends GLSurfaceView implements OnDoubleTapListener, OnGestureListener, GLSurfaceView.Renderer {
    private GestureDetector gestureDetector;
    private Context context;

    // OpenGL related global variables
    private int shaderProgramObject;

    private int modelMatrixUniform;
    private int viewMatrixUniform;
    private int projectionMatrixUniform;

    private int LaUniform, LdUniform, LsUniform;
    private int lightPositionUniform;

    private int KaUniform, KdUniform, KsUniform;
    private int materialShininessUniform;

    private int lightingEnabledUniform;

    boolean bLight = false;

    private float lightAmbient[] = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
    private float lightDiffuse[] = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float lightSpecular[] = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float lightPosition[] = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

    private float materialAmbient[] = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
    private float materialDiffuse[] = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float materialSpecular[] = new float[] { 100.0f, 100.0f, 100.0f, 1.0f };
    private float materialShininess = 50.0f;

    /*private float lightAmbient[] = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
    private float lightDiffuse[] = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float lightSpecular[] = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
    private float lightPosition[] = new float[] { 100.0f, 100.0f, 100.0f, 1.0f };

    private float materialAmbient[] = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
    private float materialDiffuse[] = new float[] { 0.5f, 0.2f, 0.7f, 1.0f };
    private float materialSpecular[] = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
    private float materialShininess = 128.0f;*/

    private float perspectiveProjectionMatrix[] = new float[16];

    private int[] vao_sphere = new int[1];
    private int[] vbo_sphere_position = new int[1];
    private int[] vbo_sphere_normal = new int[1];
    private int[] vbo_sphere_element = new int[1];

    int numVertices;
    int numElements;

    GLESView(Context _context) {
        super(_context);
        context = _context;

        setEGLContextClientVersion(3); // setting opengl version
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // gesture related code
        gestureDetector = new GestureDetector(context, this, null, false);
        gestureDetector.setOnDoubleTapListener(this);
    }

    // 3 methods of GLSurfaceView.Renderer
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // code
        String glesVersion = gl.glGetString(GL10.GL_VERSION);
        System.out.println("SVK: " + glesVersion);

        String renderer = gl.glGetString(GL10.GL_RENDERER);
        System.out.println("SVK: " + renderer);

        String glslVersion = gl.glGetString(GLES32.GL_SHADING_LANGUAGE_VERSION);
        System.out.println("SVK: " + glslVersion);

        initialize();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // code
        resize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // code
        // onDrawFrame should be considered as Game Loop
        display();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // code
        if (!gestureDetector.onTouchEvent(e)) {
            super.onTouchEvent(e);
        }
        return true;
    }

    // 3 methods of onDoubleTap Listener interface
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // code


        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // code
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // code
        if (bLight == false)
            bLight = true;
        else
            bLight = false;

        return true;
    }

    // 6 methods of onGestureListener
    @Override
    public boolean onDown(MotionEvent e) {
        // code
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //code
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // code

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // code
        uninitialize();
        System.exit(0);

        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // code
        return true;
    }

    // Custom Private Functions
    private void initialize() {
        // code
        // Vertex shader
        final String vertexShaderSourceCode = String.format(
                "#version 320 es" +
                        "\n" +
                        "in vec4 a_position;" +
                        "in vec3 a_normal;" +
                        "uniform mat4 u_modelMatrix;" +
                        "uniform mat4 u_viewMatrix;" +
                        "uniform mat4 u_projectionMatrix;" +
                        "uniform vec4 u_lightPosition;" +
                        "uniform mediump int u_lightingEnabled;" +
                        "out vec3 transformedNormals;" +
                        "out vec3 lightDirection;" +
                        "out vec3 viewerVector;" +
                        "void main(void)" +
                        "{" +
                        "if (u_lightingEnabled == 1)" +
                        "{" +
                        "vec4 eyeCoordinates = u_viewMatrix * u_modelMatrix * a_position;" +
                        "mat3 normalMatrix = mat3(u_viewMatrix * u_modelMatrix);" +
                        "transformedNormals = normalMatrix * a_normal;" +
                        "lightDirection = vec3(u_lightPosition) - eyeCoordinates.xyz;" +
                        "viewerVector = -eyeCoordinates.xyz;" +
                        "}" +
                        "gl_Position = u_projectionMatrix * u_viewMatrix * u_modelMatrix * a_position;" +
                        "}"
        );

        int vertexShaderObject = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);

        GLES32.glShaderSource(vertexShaderObject, vertexShaderSourceCode);

        GLES32.glCompileShader(vertexShaderObject);

        int status[] = new int[1];
        int infoLogLength[] = new int[1];
        String log = null;

        GLES32.glGetShaderiv(vertexShaderObject, GLES32.GL_COMPILE_STATUS, status, 0);

        if (status[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetShaderiv(vertexShaderObject, GLES32.GL_INFO_LOG_LENGTH, infoLogLength, 0);

            if (infoLogLength[0] > 0)
            {
                log = GLES32.glGetShaderInfoLog(vertexShaderObject);

                System.out.println("SVK: " + "Vertex Shader Compilation Log : " + log);

                uninitialize();

                System.exit(0);
            }
        }

        // Fragment shader
        final String fragmentShaderSourceCode = String.format(
                "#version 320 es" +
                        "\n" +
                        "precision highp float;" +
                        "in vec3 transformedNormals;" +
                        "in vec3 lightDirection;" +
                        "in vec3 viewerVector;" +
                        "uniform vec3 u_La;" +
                        "uniform vec3 u_Ld;" +
                        "uniform vec3 u_Ls;" +
                        "uniform vec3 u_Ka;" +
                        "uniform vec3 u_Kd;" +
                        "uniform vec3 u_Ks;" +
                        "uniform mediump float u_materialShininess;" +
                        "uniform mediump int u_lightingEnabled;" +
                        "out vec4 FragColor;" +
                        "void main(void)" +
                        "{" +
                        "vec3 phong_ads_light;" +
                        "if (u_lightingEnabled == 1)" +
                        "{" +
                        "vec3 ambient = u_La * u_Ka;" +
                        "vec3 normalize_transformedNormals = normalize(transformedNormals);" +
                        "vec3 normalize_lightDirection = normalize(lightDirection);" +
                        "vec3 diffuse = u_Ld * u_Kd * max(dot(normalize_lightDirection, normalize_transformedNormals), 0.0);" +
                        "vec3 reflectionVector = reflect(-normalize_lightDirection, normalize_transformedNormals);" +
                        "vec3 normalize_viewerVector = normalize(viewerVector);" +
                        "vec3 specular = u_Ls * u_Ks * pow(max(dot(reflectionVector, normalize_viewerVector), 0.0), u_materialShininess);" +
                        "phong_ads_light = ambient + diffuse + specular;" +
                        "}" +
                        "else" +
                        "{" +
                        "phong_ads_light = vec3(1.0, 1.0, 1.0);" +
                        "}" +
                        "FragColor = vec4(phong_ads_light, 1.0);" +
                        "}"
        );

        int fragmentShaderObject = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);

        GLES32.glShaderSource(fragmentShaderObject, fragmentShaderSourceCode);

        GLES32.glCompileShader(fragmentShaderObject);

        status[0] = 0;
        infoLogLength[0] = 0;
        log = null;

        GLES32.glGetShaderiv(fragmentShaderObject, GLES32.GL_COMPILE_STATUS, status, 0);

        if (status[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetShaderiv(fragmentShaderObject, GLES32.GL_INFO_LOG_LENGTH, infoLogLength, 0);

            if (infoLogLength[0] > 0)
            {
                log = GLES32.glGetShaderInfoLog(fragmentShaderObject);

                System.out.println("SVK: " + "Fragment Shader Compilation Log : " + log);

                uninitialize();

                System.exit(0);
            }
        }

        // Shader Program Object
        shaderProgramObject = GLES32.glCreateProgram();

        GLES32.glAttachShader(shaderProgramObject, vertexShaderObject);
        GLES32.glAttachShader(shaderProgramObject, fragmentShaderObject);

        // Pre Linking step
        GLES32.glBindAttribLocation(shaderProgramObject, MyGLESMacros.AMC_ATTRIBUTE_POSITION, "a_position");
        GLES32.glBindAttribLocation(shaderProgramObject, MyGLESMacros.AMC_ATTRIBUTE_NORMAL, "a_normal");

        // Linking step
        GLES32.glLinkProgram(shaderProgramObject);

        status[0] = 0;
        infoLogLength[0] = 0;
        log = null;

        GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_LINK_STATUS, status, 0);

        if (status[0] == GLES32.GL_FALSE)
        {
            GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_INFO_LOG_LENGTH, infoLogLength, 0);

            if (infoLogLength[0] > 0)
            {
                log = GLES32.glGetProgramInfoLog(shaderProgramObject);

                System.out.println("SVK: " + "Shader Program Link Log : " + log);

                uninitialize();

                System.exit(0);
            }
        }

        // Post Linking Step
        modelMatrixUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_modelMatrix");
        viewMatrixUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_viewMatrix");
        projectionMatrixUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_projectionMatrix");

        LaUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_La");
        LdUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Ld");
        LsUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Ls");
        lightPositionUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightPosition");

        KaUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Ka");
        KdUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Kd");
        KsUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_Ks");
        materialShininessUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_materialShininess");

        lightingEnabledUniform = GLES32.glGetUniformLocation(shaderProgramObject, "u_lightingEnabled");

        // declaration of vertex data arrays
        Sphere sphere = new Sphere();
        float sphere_vertices[] = new float[1146];
        float sphere_normals[] = new float[1146];
        float sphere_textures[] = new float[764];
        short sphere_elements[] = new short[2280];
        sphere.getSphereVertexData(sphere_vertices, sphere_normals, sphere_textures, sphere_elements);
        numVertices = sphere.getNumberOfSphereVertices();
        numElements = sphere.getNumberOfSphereElements();

        // vao and vbo related code
        // bind vao
        GLES32.glGenVertexArrays(1, vao_sphere, 0);
        GLES32.glBindVertexArray(vao_sphere[0]);

        // vbo for position
        GLES32.glGenBuffers(1, vbo_sphere_position, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_sphere_position[0]);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(sphere_vertices.length * 4); // 4 is size of float
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer positionBuffer = byteBuffer.asFloatBuffer();
        positionBuffer.put(sphere_vertices);
        positionBuffer.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, sphere_vertices.length * 4, positionBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(MyGLESMacros.AMC_ATTRIBUTE_POSITION, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(MyGLESMacros.AMC_ATTRIBUTE_POSITION);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

        // vbo for normal
        GLES32.glGenBuffers(1, vbo_sphere_normal, 0);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vbo_sphere_normal[0]);

        byteBuffer = ByteBuffer.allocateDirect(sphere_normals.length * 4); // 4 is size of float
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer normalBuffer = byteBuffer.asFloatBuffer();
        normalBuffer.put(sphere_normals);
        normalBuffer.position(0);

        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, sphere_normals.length * 4, normalBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glVertexAttribPointer(MyGLESMacros.AMC_ATTRIBUTE_NORMAL, 3, GLES32.GL_FLOAT, false, 0, 0);
        GLES32.glEnableVertexAttribArray(MyGLESMacros.AMC_ATTRIBUTE_NORMAL);
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0);

        // vbo for element
        GLES32.glGenBuffers(1, vbo_sphere_element, 0);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vbo_sphere_element[0]);

        byteBuffer = ByteBuffer.allocateDirect(sphere_elements.length * 2); // 2 is size of short
        byteBuffer.order(ByteOrder.nativeOrder());
        ShortBuffer elementBuffer = byteBuffer.asShortBuffer();
        elementBuffer.put(sphere_elements);
        elementBuffer.position(0);

        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, sphere_elements.length * 2, elementBuffer, GLES32.GL_STATIC_DRAW);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,0);

        // unbind vao
        GLES32.glBindVertexArray(0);

        // Depth related
        GLES32.glClearDepthf(1.0f);
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);
        GLES32.glDepthFunc(GLES32.GL_LEQUAL);

        // Enabe culling
        GLES32.glEnable(GLES32.GL_CULL_FACE);

        // Clear the Screen
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // initialize projection matrix
        Matrix.setIdentityM(perspectiveProjectionMatrix, 0);
    }

    private void resize(int width, int height) {
        // code
        if (height == 0)
            height = 1;
        GLES32.glViewport(0, 0, width, height);

        Matrix.perspectiveM(perspectiveProjectionMatrix,
                0,
                45.0f,
                (float)width / (float)height,
                0.1f,
                100.0f);
    }

    private void display() {
        // code
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        // Use The Shader Program Object
        GLES32.glUseProgram(shaderProgramObject);

        // Here there should be Graphics Done
        // Transformations
        float modelMatrix[] = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        float viewMatrix[] = new float[16];
        Matrix.setIdentityM(viewMatrix, 0);
        float translationmatrix[] = new float[16];
        Matrix.setIdentityM(translationmatrix, 0);
        Matrix.translateM(translationmatrix, 0, 0.0f, 0.0f, -2.0f);
        modelMatrix = translationmatrix;

        GLES32.glUniformMatrix4fv(modelMatrixUniform, 1, false, modelMatrix, 0);
        GLES32.glUniformMatrix4fv(viewMatrixUniform, 1, false, viewMatrix, 0);
        GLES32.glUniformMatrix4fv(projectionMatrixUniform, 1, false, perspectiveProjectionMatrix, 0);

        // Sending Light Related Data
        if (bLight == true)
        {
            GLES32.glUniform1i(lightingEnabledUniform, 1);

            GLES32.glUniform3fv(LaUniform, 1, lightAmbient, 0);
            GLES32.glUniform3fv(LdUniform, 1, lightDiffuse,0);
            GLES32.glUniform3fv(LsUniform, 1, lightSpecular,0);
            GLES32.glUniform4fv(lightPositionUniform, 1, lightPosition, 0);

            GLES32.glUniform3fv(KaUniform, 1, materialAmbient, 0);
            GLES32.glUniform3fv(KdUniform, 1, materialDiffuse, 0);
            GLES32.glUniform3fv(KsUniform, 1, materialSpecular, 0);
            GLES32.glUniform1f(materialShininessUniform, materialShininess);
        }
        else
            GLES32.glUniform1i(lightingEnabledUniform, 0);

        // bind vao
        GLES32.glBindVertexArray(vao_sphere[0]);

        // Draw
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, vbo_sphere_element[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, numElements, GLES32.GL_UNSIGNED_SHORT, 0);

        // unbind vao
        GLES32.glBindVertexArray(0);

        // Unuse the Shader Program Object
        GLES32.glUseProgram(0);

        requestRender(); // swapbuffers
    }

    private void uninitialize() {
        // code
        // shader uninitialization
        if (shaderProgramObject > 0)
        {
            int retVal[] = new int[1];
            GLES32.glUseProgram(shaderProgramObject);

            GLES32.glGetProgramiv(shaderProgramObject, GLES32.GL_ATTACHED_SHADERS, retVal, 0);

            if (retVal[0] > 0)
            {
                int numAttachedShaders = retVal[0];

                int shaderObjects[] = new int[numAttachedShaders];

                GLES32.glGetAttachedShaders(shaderProgramObject, numAttachedShaders, retVal, 0, shaderObjects, 0);

                for(int i = 0; i < numAttachedShaders; i++)
                {
                    GLES32.glDetachShader(shaderProgramObject, shaderObjects[i]);

                    GLES32.glDeleteShader(shaderObjects[i]);

                    shaderObjects[i] = 0;
                }
            }

            GLES32.glUseProgram(0);

            GLES32.glDeleteProgram(shaderProgramObject);

            shaderProgramObject = 0;
        }

        // Deletion and Uninitialization of vbo
        if (vbo_sphere_position[0] > 0)
        {
            GLES32.glDeleteBuffers(1, vbo_sphere_position, 0);
            vbo_sphere_position[0] = 0;
        }

        if (vbo_sphere_normal[0] > 0)
        {
            GLES32.glDeleteBuffers(1, vbo_sphere_normal, 0);
            vbo_sphere_normal[0] = 0;
        }

        if (vbo_sphere_element[0] > 0)
        {
            GLES32.glDeleteBuffers(1, vbo_sphere_element, 0);
            vbo_sphere_element[0] = 0;
        }

        // Deletion and Uninitialization of vao
        if (vao_sphere[0] > 0)
        {
            GLES32.glDeleteVertexArrays(1, vao_sphere, 0);
            vao_sphere[0] = 0;
        }
    }

}
