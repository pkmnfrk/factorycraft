package com.mike_caron.factorycraft.util;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class MathUtil
{
    private MathUtil(){}

    public static void applyMatrix(final Vector3f[] src, Vector3f[] dest, Matrix4f m)
    {
        Vector4f tmp = new Vector4f();
        tmp.setW(1f);
        for(int i = 0; i < src.length; i++)
        {
            tmp.set(src[i].x, src[i].y, src[i].z);
            Matrix4f.transform(m, tmp, tmp);
            if(dest[i] == null) dest[i] = new Vector3f();
            dest[i].set(tmp);
        }

    }

    public static float distance(Vector3f a, Vector3f b)
    {
        return (float)Math.sqrt(
              Math.pow(b.x - a.x, 2)
            + Math.pow(b.y - a.y, 2)
            + Math.pow(b.z - a.z, 2)
        );
    }

    public static float angle(Vector3f origin, Vector3f point, Vector3f dest)
    {
        origin = normalize(origin);
        point = normalize(point);

        if(origin.equals(point))
        {
            return 0f;
        }

        Vector3f.cross(origin, point, dest);
        if(dest.length() == 0)
        {
            dest.setX(1f);
        }

        normalize(dest, dest);

        return (float)Math.acos(Vector3f.dot(origin, point));
    }

    public static Vector3f normalize(Vector3f vector3f)
    {
        Vector3f ret = new Vector3f();
        normalize(vector3f, ret);
        return ret;
    }

    public static void normalize(Vector3f vector3f, Vector3f dest)
    {
        float l = vector3f.length();

        if(l == 0 || l == 1)
        {
            dest.set(vector3f);
        }
        else
        {
            dest.setX(vector3f.x / l);
            dest.setY(vector3f.y / l);
            dest.setZ(vector3f.z / l);
        }
    }
}
