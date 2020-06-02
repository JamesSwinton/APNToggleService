package com.zebra.jamesswinton.apntoggleservice;

import android.app.Application;

public class App extends Application {

  // Constants
  public static final String INTERNET_APN_PROFILE_NAME = "InternetApnProfile";
  public static final String INTERNET_APN_PROFILE_XML =
                  "<wap-provisioningdoc>\n" +
                  "  <characteristic type=\"Profile\">\n" +
                  "    <parm name=\"ProfileName\" value=\"InternetApnProfile\"/>\n" +
                  "    <characteristic version=\"4.2\" type=\"GprsMgr\">\n" +
                  "      <parm name=\"GprsAction\" value=\"1\" />\n" +
                  "      <parm name=\"GprsCarrier\" value=\"0\" />\n" +
                  "      <characteristic type=\"gprs-details\">\n" +
                  "        <parm name=\"ApnName\" value=\"activeapn\" />\n" +
                  "        <parm name=\"ReplaceIfExisting\" value=\"1\" />\n" +
                  "        <parm name=\"MakeDefault\" value=\"1\" />\n" +
                  "      </characteristic>\n" +
                  "      <characteristic type=\"custom-details\">\n" +
                  "        <parm name=\"CustomAccessPoint\" value=\"internet\" />\n" +
                  "        <parm name=\"CustomUserName\" value=\"\" />\n" +
                  "        <parm name=\"CustomPassword\" value=\"&amp;#1270&amp;#127C2fhOrjKcM6DIGCIVmhrf1YgP4qe27H90alboqf0qDOg7WwTzzWX7ookk2C94RfXJyvD0PHJchrbHEYPGJW6eCO3qYxcx424Qs1t4TIwdS8Fsle9FtA+odJhVaazK7jHAz5Xp/R1KBXn4NggR7VxS+LY4z2RXupMSphEY4U2SkcVuByn1CyZs7sWoTAXtS2XOrUTllldkuHpnU1fBXijkLaH2yN5MU8jlgOwakeBgpl+2TlqQmeJiy/Z54mZl47p6WcSv4IyygmKdL/9e3nW00ilLx8s/B33Gq3ayoEy1GnRsfhoLI61we375xlDoYVAPWpQfEBDgfb7nBE23+DdLg==\" />\n" +
                  "      </characteristic>\n" +
                  "    </characteristic>\n" +
                  "  </characteristic>" +
                  "</wap-provisioningdoc>";

  public static final String MOBILEDADE_APN_PROFILE_NAME = "MobildadeApnProfile";
  public static final String MOBILEDADE_APN_PROFILE_XML =
          "<wap-provisioningdoc>\n" +
          "  <characteristic type=\"Profile\">\n" +
          "    <parm name=\"ProfileName\" value=\"MobildadeApnProfile\"/>\n" +
          "    <characteristic version=\"4.2\" type=\"GprsMgr\">\n" +
          "      <parm name=\"GprsAction\" value=\"1\" />\n" +
          "      <parm name=\"GprsCarrier\" value=\"0\" />\n" +
          "      <characteristic type=\"gprs-details\">\n" +
          "        <parm name=\"ApnName\" value=\"activeapn\" />\n" +
          "        <parm name=\"ReplaceIfExisting\" value=\"1\" />\n" +
          "        <parm name=\"MakeDefault\" value=\"1\" />\n" +
          "      </characteristic>\n" +
          "      <characteristic type=\"custom-details\">\n" +
          "        <parm name=\"CustomAccessPoint\" value=\"delta-outsys.meo.pt\" />\n" +
          "        <parm name=\"CustomUserName\" value=\"\" />\n" +
          "        <parm name=\"CustomPassword\" value=\"&amp;#1270&amp;#127ZX4oSZ3NnBiKwMfUmdBWzWN2Utkb8ONo7b7mlWCAASTQqS4Ea1EtArmEnx0jkQc/j+AujfyLhsWRO8iaX+U8CE1s01pFRhL3j+l7e9OYRfoAgTinFHAeAMv40wPIhgiowPSzml056owpsEZfVjRsXpL38GGF//5+C2j4TKlKaMQ7fOIqZfca15xPy2tf1jTQhGUSvHnyLXAHaF58IwYZNzD3iMZ1YrrdL6+zyeIm29s03EaMSrv072gmu/X7mQUeXLsUXRxsk1jtJNEwANnzZx/B7pd33qVP4f8jWaBxtvqLVeAorc2TtmG6Vc94DYl6m9Ti7xHrp6/HUGlj2xhdTQ==\" />\n" +
          "      </characteristic>\n" +
          "    </characteristic>\n" +
          "  </characteristic>" +
          "</wap-provisioningdoc>";
}
