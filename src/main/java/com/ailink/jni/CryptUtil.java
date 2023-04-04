package com.ailink.jni;

public class CryptUtil {
	static {
			System.loadLibrary("jnicrypto");
	}

    public static native void DeriveKeysSeafAmf(byte[] ausf, byte[] supi, byte[] snn, byte[] abba, byte[] kseaf, byte[] kamf);
    public static native void DeriveNasKeys(byte[] kamf, byte[] kNasEnc, byte[] kNasInt, int ciphering, int integrity);
    public static native void DeriveNasKeysTest();

    public static native int ComputeMacUia2(byte[] pKey, int count, int fresh, int dir, byte[] pData);
    public static native int ComputeMacEia1(byte[] pKey, int count, int fresh, int dir, byte[] pData);
    public static native int ComputeMacEia2(byte[] pKey, int count, int fresh, int dir, byte[] pData);
    public static native int ComputeMacEia3(byte[] pKey, int count, int fresh, int dir, byte[] pData);
	
    public static native void EncryptEea1(byte[] pKey, int count, int fresh, int dir, byte[] pData);
    public static native void DecryptEea1(byte[] pKey, int count, int fresh, int dir, byte[] pData);

    public static native void EncryptEea2(byte[] pKey, int count, int fresh, int dir, byte[] pData);
    public static native void DecryptEea2(byte[] pKey, int count, int fresh, int dir, byte[] pData);

    public static native void EncryptEea3(byte[] pKey, int count, int fresh, int dir, byte[] pData);
    public static native void DecryptEea3(byte[] pKey, int count, int fresh, int dir, byte[] pData);
}
