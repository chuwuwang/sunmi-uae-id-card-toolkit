package ae.emiratesid.idcard.toolkit.sample;

import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import ae.emiratesid.idcard.toolkit.CardReader;
import ae.emiratesid.idcard.toolkit.Toolkit;
import ae.emiratesid.idcard.toolkit.ToolkitException;
import ae.emiratesid.idcard.toolkit.datamodel.ToolkitResponse;
import ae.emiratesid.idcard.toolkit.sample.logger.Logger;
import ae.emiratesid.idcard.toolkit.sample.utils.CryptoUtils;
import ae.emiratesid.idcard.toolkit.sample.utils.RequestGenerator;

/**
 * This class will manage connection .
 * Creates a connection with reader and retain that connection until the app is closed or
 * connection is force fully closed.
 * <p/>
 * NOTE : The connection should be managed accordingly to your app requirement.
 * The connection should be properly close when  the app is not in foreground  or it don't need the connection.
 * This allow other application to  use the Hardware devices (Readers).
 */
public class ConnectionController {

    private static CardReader cardReader = null;
    private static Toolkit toolkit = null;

    public static boolean initialize() throws ToolkitException {
        if (toolkit == null) {
            try {
                String stringConfigPath = AppController.path;
                Logger.d("VG URL ___initialize()" + AppController.VG_URL);
                Logger.d("config Path____ " + stringConfigPath);
                Logger.d("set configPath Success");
                Context context = AppController.getContext();
                StringBuilder configBuilder = new StringBuilder();
                configBuilder.append("\n" + "config_directory =" + AppController.path);
                configBuilder.append("\n" + "log_directory =" + stringConfigPath);
                configBuilder.append("\n" + "read_publicdata_offline = true");
                //configBuilder.append("\n" + "config_tls_cert = LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlHSkRDQ0JReWdBd0lCQWdJUkFJMGN2Y1l1VFBxaWpEeThMK1NaTm5zd0RRWUpLb1pJaHZjTkFRRUxCUUF3DQpnWTh4Q3pBSkJnTlZCQVlUQWtkQ01Sc3dHUVlEVlFRSUV4SkhjbVZoZEdWeUlFMWhibU5vWlhOMFpYSXhFREFPDQpCZ05WQkFjVEIxTmhiR1p2Y21ReEdEQVdCZ05WQkFvVEQxTmxZM1JwWjI4Z1RHbHRhWFJsWkRFM01EVUdBMVVFDQpBeE11VTJWamRHbG5ieUJTVTBFZ1JHOXRZV2x1SUZaaGJHbGtZWFJwYjI0Z1UyVmpkWEpsSUZObGNuWmxjaUJEDQpRVEFlRncweU1qQTJNREl3TURBd01EQmFGdzB5TXpBMk1ESXlNelU1TlRsYU1CTXhFVEFQQmdOVkJBTU1DQ291DQpaV1JqTG1GbE1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBMkdPSmxqRkhGL0YvDQpkQ3p3QXNhYkZXMVRBL0gvRDNsUFBiRUVSMFlpMU8wS2Roa2paa1BmR1d0cGpWZUxPY0VMRmgrRTQzRkxNVGwvDQpmdHVFaVlBV01mcitLeGdiZWpTYzJJU3RrR1BlamxWSTZnTUdkZWh4Wm1BaVdWNnVJNVdDV2NkRitXWWZSVTF2DQpHQnlsQWN6RFd1WXQ3bHlEQmVmekNNSVRnUUN1VHpKVVViSGYrV1NRZkRQak5STWJXS2NIYWppM1lTempaby9FDQovajJuYXY2RXhMTG1pZ1VTTDNQaEI0dFNaOEZTeDFnOGcxbE5DTldmZ0cxMXBxT2lwM3VjY0pPckU3OWcyaStiDQptQjN5NXFvZ1l1b2NWMEF6aHYxN3JMcnFXdy9oRkZBcnZheFZGRzBGRHBmc0dKV3hrMFdJclNZLzF0R08rV2xLDQpMcGJOd3dSZlRRSURBUUFCbzRJQzlEQ0NBdkF3SHdZRFZSMGpCQmd3Rm9BVWpZeGV4RlN0aXVGMzZadjVtd1hoDQp1QUdOWWVFd0hRWURWUjBPQkJZRUZCbkxlUnRoMGlUMEVxQWNCMnlXRTVFUVFsMWpNQTRHQTFVZER3RUIvd1FFDQpBd0lGb0RBTUJnTlZIUk1CQWY4RUFqQUFNQjBHQTFVZEpRUVdNQlFHQ0NzR0FRVUZCd01CQmdnckJnRUZCUWNEDQpBakJKQmdOVkhTQUVRakJBTURRR0N5c0dBUVFCc2pFQkFnSUhNQ1V3SXdZSUt3WUJCUVVIQWdFV0YyaDBkSEJ6DQpPaTh2YzJWamRHbG5ieTVqYjIwdlExQlRNQWdHQm1lQkRBRUNBVENCaEFZSUt3WUJCUVVIQVFFRWVEQjJNRThHDQpDQ3NHQVFVRkJ6QUNoa05vZEhSd09pOHZZM0owTG5ObFkzUnBaMjh1WTI5dEwxTmxZM1JwWjI5U1UwRkViMjFoDQphVzVXWVd4cFpHRjBhVzl1VTJWamRYSmxVMlZ5ZG1WeVEwRXVZM0owTUNNR0NDc0dBUVVGQnpBQmhoZG9kSFJ3DQpPaTh2YjJOemNDNXpaV04wYVdkdkxtTnZiVEFiQmdOVkhSRUVGREFTZ2dncUxtVmtZeTVoWllJR1pXUmpMbUZsDQpNSUlCZ0FZS0t3WUJCQUhXZVFJRUFnU0NBWEFFZ2dGc0FXb0Fkd0N0OTc3NmZQOFF5SXVkUFp3ZVBoaHF0R2NwDQpYYyt4RENUS2hZWTA2OXlDaWdBQUFZRWozd2lBQUFBRUF3QklNRVlDSVFDYmUvK1V2SExTbHhUVWRWWnN6NytiDQp1QWh6TWlyb1lra0ZkdHlWaHQ5ZktBSWhBS3FFalE0YVkzR0FRWm1aVHpycUhHTE9sUU9GSlY2RklvbzY0TDlZDQo1NHpkQUhjQWVqS01WTmkzTGJZZzZqamdVaDdwaEJad01oT0ZUVHZTSzhFNlY2TlM2MUlBQUFHQkk5OElhZ0FBDQpCQU1BU0RCR0FpRUFzbkVES29FR01GdUpRdUlkTXZUNkoySUdJMk5hTnN2T3ZPY2c3VFZYcHFrQ0lRQ3dJNzZEDQo2L09Ia1JZRFUvTTFRWnFiMVl6bzJOcm16dEc2NGg1RHZ4OXpxZ0IyQU9nKzBObys5UVkxTXVkWEtMeUphOGtEDQowOHZSRVd2czYybmhkMzF0QnIxdUFBQUJnU1BmQ0RjQUFBUURBRWN3UlFJaEFKRFQzWnF1Mm9kK2REMlFaTE1oDQo4S3BPbEw3aGoveWozWStPMktMSFlKQjZBaUE2U1NEVURvaGR2dnhjWVBITjNmUVdwOHJKRHV3Q3NXVFVDMXRGDQpEWGVDbERBTkJna3Foa2lHOXcwQkFRc0ZBQU9DQVFFQXhVcTZtODh3YXVJcC9ZWm9tUkdENExUeG5DRVFpWE9oDQpsRWZSc3RkZ2NldjdhZWxJUFRRcTFtQStUTlM4WmNTcmVqekRBNHVQaC96eWVRc0ZUTzFYTzhsWHJ4blZuZ2xkDQpqeEhlSlJLVjMydncraHNuRkdTZ2NSUW4yeGt3MVBXaVA3ZnFzZXlLdDMxM1h4OU1IVmVJUUJlZWd0ejAxbkZXDQo2S1l1UTJ1ZmZqbThyd05BallVZTNYWGlub0U1Mmc1Si9VRmhQT1ZYd2hpUFVZeWhHU2hDZ0FGL2RJV1h5NVBTDQprNGxSeGZtV01vUktjZHBHZ0ovZDhWU2RKOWg2ZkZ0TXVHNEYzVk9SNXFXTzB5Mk5aQXJNL1dTNW9QcEZyNUl5DQp5Y2xWb0JsOWxXbVovR1NQTFRFaElNN2hCeHZQV00vQkdYaG5yendwYXZRZTF1UWZ5bjVrblE9PQ0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQ0K");
                //configBuilder.append("\n" + "config_tls_cert_chain = LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlHRXpDQ0EvdWdBd0lCQWdJUWZWdFJKclIydWhIYmRCWUx2Rk1OcHpBTkJna3Foa2lHOXcwQkFRd0ZBRENCDQppREVMTUFrR0ExVUVCaE1DVlZNeEV6QVJCZ05WQkFnVENrNWxkeUJLWlhKelpYa3hGREFTQmdOVkJBY1RDMHBsDQpjbk5sZVNCRGFYUjVNUjR3SEFZRFZRUUtFeFZVYUdVZ1ZWTkZVbFJTVlZOVUlFNWxkSGR2Y21zeExqQXNCZ05WDQpCQU1USlZWVFJWSlVjblZ6ZENCU1UwRWdRMlZ5ZEdsbWFXTmhkR2x2YmlCQmRYUm9iM0pwZEhrd0hoY05NVGd4DQpNVEF5TURBd01EQXdXaGNOTXpBeE1qTXhNak0xT1RVNVdqQ0JqekVMTUFrR0ExVUVCaE1DUjBJeEd6QVpCZ05WDQpCQWdURWtkeVpXRjBaWElnVFdGdVkyaGxjM1JsY2pFUU1BNEdBMVVFQnhNSFUyRnNabTl5WkRFWU1CWUdBMVVFDQpDaE1QVTJWamRHbG5ieUJNYVcxcGRHVmtNVGN3TlFZRFZRUURFeTVUWldOMGFXZHZJRkpUUVNCRWIyMWhhVzRnDQpWbUZzYVdSaGRHbHZiaUJUWldOMWNtVWdVMlZ5ZG1WeUlFTkJNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DDQpBUThBTUlJQkNnS0NBUUVBMW5NejF0YzhJTkFBMGhkRnVOWStCNkkveDBIdU1qREpzR3o5OUovTEVwZ1BMVCtODQpUUUVNZ2c4WGYySXU2YmhJZWZzV2cwNnQxeklsazdjSHY3bFFQNmxNdzBBcTZUbi8yWUhLSHhZeVFkcUFKcmtqDQplb2NnSHVQL0lKbzhsVVJ2aDNVR2tFQzBNcE1XQ1JBSUl6N1MzWWNQYjExUkZHb0thY1ZQQVhKcHo5T1RURzBFDQpvS01iZ242eG1ybnR4WjdGTjNpZm1nZzArMVl1V01RSkRnWmtXN3czM1BHZktHaW9WckNTbzF5ZnU0aVlDQnNrDQpIYXN3aGE2dnNDNmVlcDNCd0VJYzRnTHc2dUJLMHUrUURyVEJRQmJ3YjRWQ1NtVDNwRENnL3I4dW95ZGFqb3RZDQp1SzNER1JlRVkrMXZWdjJEeTJBMHhIUys1cDNiNGVUbHlneGZGUUlEQVFBQm80SUJiakNDQVdvd0h3WURWUjBqDQpCQmd3Rm9BVVUzbS9XcW9yU3M5VWdPSFltOENkOHJJRFpzc3dIUVlEVlIwT0JCWUVGSTJNWHNSVXJZcmhkK21iDQorWnNGNGJnQmpXSGhNQTRHQTFVZER3RUIvd1FFQXdJQmhqQVNCZ05WSFJNQkFmOEVDREFHQVFIL0FnRUFNQjBHDQpBMVVkSlFRV01CUUdDQ3NHQVFVRkJ3TUJCZ2dyQmdFRkJRY0RBakFiQmdOVkhTQUVGREFTTUFZR0JGVWRJQUF3DQpDQVlHWjRFTUFRSUJNRkFHQTFVZEh3UkpNRWN3UmFCRG9FR0dQMmgwZEhBNkx5OWpjbXd1ZFhObGNuUnlkWE4wDQpMbU52YlM5VlUwVlNWSEoxYzNSU1UwRkRaWEowYVdacFkyRjBhVzl1UVhWMGFHOXlhWFI1TG1OeWJEQjJCZ2dyDQpCZ0VGQlFjQkFRUnFNR2d3UHdZSUt3WUJCUVVITUFLR00yaDBkSEE2THk5amNuUXVkWE5sY25SeWRYTjBMbU52DQpiUzlWVTBWU1ZISjFjM1JTVTBGQlpHUlVjblZ6ZEVOQkxtTnlkREFsQmdnckJnRUZCUWN3QVlZWmFIUjBjRG92DQpMMjlqYzNBdWRYTmxjblJ5ZFhOMExtTnZiVEFOQmdrcWhraUc5dzBCQVF3RkFBT0NBZ0VBTXI5aHZRNUl3MC9IDQp1a2ROK0p4NEdRSGNFeDJBYi96RGNMUlNtakV6bWxkUyt6R2VhNlR2VktxSmpVQVhhUGdSRUh6U3lySHhWWWJIDQo3ck0ya1liMk9WRy9ScjhQb0xxMDkzNUp4Q28yRjU3a2FEbDZyNVJPVm0reWV6dS9Db2E5emNWM0hBTzRPTEdpDQpIMTkrMjRyY1JraTJhQXJQc3JXMDRqVGtaNms0WmdsZTByajhuU2c2RjBBbnduSk9LZjBoUEh6UEUvdVdMTVV4DQpSUDBUN2RXYnFXbG9kM3p1NGYraytUWTRDRk01b29RMG5Cbnp2ZzZzMVNRMzZ5T29lTkRUNSsrU1IyUmlPU0x2DQp4dmNSdmlLRnhtWkVKQ2FPRURLTnlKT3VCNTZEUGkvWitmVkdqbU8rd2VhMDNLYk5JYWlHQ3BYWkxvVW1HdjM4DQpzYlpYUW0yVjBUUDJPUlFHZ2tFNDlZOVkzSUJicE5WOWxYajlwNXYvL2NXb2Fhc201NmVrQllkYnFiZTRveUFMDQpsNmxGaGQyemkrV0pONDRwRGZ3R0YvWTRRQTVDNUJJRyszdnp4aEZvWXQvam1QUVQyQlZQaTdGcDJSQmd2R1FxDQo2akczNUxXak9oU2JKdU1MZS8wQ2pyYVp3VGlYV1RiMnFIU2loclplNjhaazZzK2dvL2x1bnJvdEViYUdtQWhZDQpMY21zSldUeVhuVzBPTUd1ZjFwR2crcFJ5cmJ4bVJFMWE2VnFlOFlBc09mNHZtU3lyY2pDOGF6alVlcWtrK0I1DQp5T0dCUU1rS1crRVNQTUZnS3VPWHdJbEN5cFRQUnBnU2FidVkwTUxURFhKTFIyN2xrOFF5S0dPSFErU3dNajRLDQowMHUvSTVzVUtVRXJtZ1Fma3kzeHh6bElQSzFhRW44PQ0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQ0KLS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlGZ1RDQ0JHbWdBd0lCQWdJUU9YSkVPdmtpdDFIWDAyd1EzVEUxbFRBTkJna3Foa2lHOXcwQkFRd0ZBREI3DQpNUXN3Q1FZRFZRUUdFd0pIUWpFYk1Ca0dBMVVFQ0F3U1IzSmxZWFJsY2lCTllXNWphR1Z6ZEdWeU1SQXdEZ1lEDQpWUVFIREFkVFlXeG1iM0prTVJvd0dBWURWUVFLREJGRGIyMXZaRzhnUTBFZ1RHbHRhWFJsWkRFaE1COEdBMVVFDQpBd3dZUVVGQklFTmxjblJwWm1sallYUmxJRk5sY25acFkyVnpNQjRYRFRFNU1ETXhNakF3TURBd01Gb1hEVEk0DQpNVEl6TVRJek5UazFPVm93Z1lneEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUlFd3BPWlhjZ1NtVnljMlY1DQpNUlF3RWdZRFZRUUhFd3RLWlhKelpYa2dRMmwwZVRFZU1Cd0dBMVVFQ2hNVlZHaGxJRlZUUlZKVVVsVlRWQ0JPDQpaWFIzYjNKck1TNHdMQVlEVlFRREV5VlZVMFZTVkhKMWMzUWdVbE5CSUVObGNuUnBabWxqWVhScGIyNGdRWFYwDQphRzl5YVhSNU1JSUNJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBZzhBTUlJQ0NnS0NBZ0VBZ0JKbEZ6WU93OXNJDQpzOUNzVncxMjdjMG4wMHl0VUlOaDRxb2dUUWt0WkFuY3pvbWZ6RDJwN1BiUHdkengwN0hXZXpjb0VTdEgyam5HDQp2RG9adEYrbXZYMmRvMk5DdG5ieXFUc3JrZmppYjlEc0ZpQ1FDVDdpNkhUSkdMU1IxR0prMjMrakJ2R0lHR3FRDQpJank4L2hQd2h4Ujc5dVFmanRUa1VjWVJaMFlJVWN1R0ZGUS92RFArZm15Yy94YWRHTDFSampXbXAyYkljbWZiDQpJV2F4MUp0NEE4QlFPdWpNOE55OG5reityd1dXTlI5WFdyZi96dms5dHl5MjlsVGR5T2NTT2sydVRJcTNYSnEwDQp0eUE5eW44aU5LNStPMmhtQVVUbkFVNUdVNXN6WVBlVXZsTTNrSE5EOHpMRFUrL2JxdjUwVG1uSGE0eGdrOTdFDQp4d3pmNFRLdXpKTTdVWGlWWjR2dVBWYitETkJwRHhzUDh5VW1hek50OTI1SCtuTkQ1WDRPcFdheEtYd3loR05WDQppY1FOd1pOVU1Ca1RyTk45TjZmclhUcHNOVnpiUWRjUzJxbEpDOS9ZZ0lvSmsyS090V2JQSllqTmhMaXhQNlE1DQpEOWtDbnVzU1RKVjg4MnNGcVY0V2c4eTRaK0xvRTUzTVc0TFRUTFB0Vy8vZTVYT3NJenN0QUw4MVZYUUpTZGhKDQpXQnAva2pibVVaSU84eVo5SEUwWHZNbnNReWJRdjBGZlFLbEVSUFNaNTFlSG5sQWZWMVNvUHYxMFl5K3hVR1VKDQo1bGhDTGtNYVRMVHdKVWRaK2dRZWs5UW1Sa3BRZ2JMZXZuaTMvR2NWNGNsWGhCNFBZOWJwWXJyV1gxVXU2bHpHDQpLQWdFSlRtNERpdXA4a3lYSEFjL0RWTDE3ZTh2Z2c4Q0F3RUFBYU9COGpDQjd6QWZCZ05WSFNNRUdEQVdnQlNnDQpFUW9qUHBieEIremlyeW52Z3FWLzBEQ2t0REFkQmdOVkhRNEVGZ1FVVTNtL1dxb3JTczlVZ09IWW04Q2Q4cklEDQpac3N3RGdZRFZSMFBBUUgvQkFRREFnR0dNQThHQTFVZEV3RUIvd1FGTUFNQkFmOHdFUVlEVlIwZ0JBb3dDREFHDQpCZ1JWSFNBQU1FTUdBMVVkSHdROE1Eb3dPS0Eyb0RTR01taDBkSEE2THk5amNtd3VZMjl0YjJSdlkyRXVZMjl0DQpMMEZCUVVObGNuUnBabWxqWVhSbFUyVnlkbWxqWlhNdVkzSnNNRFFHQ0NzR0FRVUZCd0VCQkNnd0pqQWtCZ2dyDQpCZ0VGQlFjd0FZWVlhSFIwY0RvdkwyOWpjM0F1WTI5dGIyUnZZMkV1WTI5dE1BMEdDU3FHU0liM0RRRUJEQVVBDQpBNElCQVFBWWgxSGNkQ0U5bklyZ0o3Y3owQzdNN1BEbXkxNFIzaUp2bTNXT25uTCs1TmIrcWgrY2xpM3ZBMHArDQpydlNOYjNJOFF6dkFQK3U0MzF5cXFjYXU4dnpZN3FON1EvYUdObndVNE0zMDl6LyszcmkwaXZDUmx2NzlRMlIrDQovY3pTQWFGOWZmZ1pHY2xDS3hPL1dJdTZwS0ptQkhhSWtVNE1pUlRPb2szSk1yTzY2QlFhdkhIeFcvQkJDNWdBDQpDaUlERU9VTXNmbk5ramNaN1R2eDVEcTIrVVVUSm5XdnU2cnZQM3QzTzlMRUFwRTlHUURURjF3NTJ6OTdHQTFGDQp6Wk9GbGk5ZDMxa1dUejlSdmRWRkdEL3RTbzdvQm1GMEl4YTFEVkJ6SjBSSGZ4QmRpU3ByaFRFVXhPaXBha3lBDQp2R3A0ejdoL2puWnltUXlkL3RlUkNCYWhvMStWDQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tDQotLS0tLUJFR0lOIENFUlRJRklDQVRFLS0tLS0NCk1JSUVNakNDQXhxZ0F3SUJBZ0lCQVRBTkJna3Foa2lHOXcwQkFRVUZBREI3TVFzd0NRWURWUVFHRXdKSFFqRWINCk1Ca0dBMVVFQ0F3U1IzSmxZWFJsY2lCTllXNWphR1Z6ZEdWeU1SQXdEZ1lEVlFRSERBZFRZV3htYjNKa01Sb3cNCkdBWURWUVFLREJGRGIyMXZaRzhnUTBFZ1RHbHRhWFJsWkRFaE1COEdBMVVFQXd3WVFVRkJJRU5sY25ScFptbGoNCllYUmxJRk5sY25acFkyVnpNQjRYRFRBME1ERXdNVEF3TURBd01Gb1hEVEk0TVRJek1USXpOVGsxT1Zvd2V6RUwNCk1Ba0dBMVVFQmhNQ1IwSXhHekFaQmdOVkJBZ01Fa2R5WldGMFpYSWdUV0Z1WTJobGMzUmxjakVRTUE0R0ExVUUNCkJ3d0hVMkZzWm05eVpERWFNQmdHQTFVRUNnd1JRMjl0YjJSdklFTkJJRXhwYldsMFpXUXhJVEFmQmdOVkJBTU0NCkdFRkJRU0JEWlhKMGFXWnBZMkYwWlNCVFpYSjJhV05sY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVANCkFEQ0NBUW9DZ2dFQkFMNUFuZlJ1NGVwMmh4eE5SVVNPdmtiSWd3YWR3U3IrR0IrTzVBTDY4NnRkVUlvV01RdWENCkJ0REZjQ0xOU1MxVVk4eTJibWhHQzFQcXkwd2t3THh5VHVyeEZhNzBWSm9TQ3NONnNqTmc0dHFKVmZNaVdQUGUNCjNNL3ZnNGFpakpSUG4yanltSkJHaENmSGRyL2p6RFVzaTE0SFpHV0N3RWl3cUpINVlaOTJJRkNva2NkbXRldDQNCllnTlc4SW9hRStveG94NmdtZjA0OXZZbk1saHZCL1ZydVBzVUs2KzNxc3pXWTE5empOb0ZtYWc0cU1zWGVEWlINCnJPbWU5SGc2amM4UDJVTGltQXlyTDU4T0FkN3ZuNWxKOFMzZnJIUk5HNWkxUjhYbEtkSDVrQmpIWXB5K2c4Y20NCmV6NktKY2ZBM1ozbU5XZ1FJSjJQMk43U3c0U2NEVjdvTDhrQ0F3RUFBYU9Cd0RDQnZUQWRCZ05WSFE0RUZnUVUNCm9CRUtJejZXOFFmczRxOHA3NEtsZjlBd3BMUXdEZ1lEVlIwUEFRSC9CQVFEQWdFR01BOEdBMVVkRXdFQi93UUYNCk1BTUJBZjh3ZXdZRFZSMGZCSFF3Y2pBNG9EYWdOSVl5YUhSMGNEb3ZMMk55YkM1amIyMXZaRzlqWVM1amIyMHYNClFVRkJRMlZ5ZEdsbWFXTmhkR1ZUWlhKMmFXTmxjeTVqY213d05xQTBvREtHTUdoMGRIQTZMeTlqY213dVkyOXQNCmIyUnZMbTVsZEM5QlFVRkRaWEowYVdacFkyRjBaVk5sY25acFkyVnpMbU55YkRBTkJna3Foa2lHOXcwQkFRVUYNCkFBT0NBUUVBQ0ZiOEF2Q2I2UCtrK3RaN3hrU0F6ay9FeGZZQVdNeW10cndVU1dnRWR1am03bDNzQWc5ZzFvMVENCkdFOG1UZ0hqNXJDbDdyKzhkRlJCdi8zOEVyakhUMXIwaVdBRmYyQzNCVXJ6OXZIQ3Y4UzVkSWEyTFgxcnpOTHoNClJ0MHZ4dUJxdzhNMEF5eDlsdDFhd2c2bkNwbkJCWXVyREMvelhEclBiRGRWQ1lmZVUwQnNXTy84dHF0bGJnVDINCkc5dzg0Rm9WeHA3WjhWbElNQ0ZsQTJ6czZTRno3SnNEb2VBM3JhQVZHSS82dWdMT3B5eXBFQk1zMU9VSUpxc2kNCmwyRDRrRjUwMUtLYVU3M3lxV2pnb203QzEyeXhvdytldit0bzUxYnlydkxqS3pnNkNZRzFhNFhYdmkzdFB4cTMNCnNtUGk5V0lzZ3RScUFFRlE4VG1EbjVYcE5wYVliZz09DQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tDQo=");

                //configBuilder.append("\n" + "config_tls_cert = LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUdKRENDQlF5Z0F3SUJBZ0lSQUkwY3ZjWXVUUHFpakR5OEwrU1pObnN3RFFZSktvWklodmNOQVFFTEJRQXcKZ1k4eEN6QUpCZ05WQkFZVEFrZENNUnN3R1FZRFZRUUlFeEpIY21WaGRHVnlJRTFoYm1Ob1pYTjBaWEl4RURBTwpCZ05WQkFjVEIxTmhiR1p2Y21ReEdEQVdCZ05WQkFvVEQxTmxZM1JwWjI4Z1RHbHRhWFJsWkRFM01EVUdBMVVFCkF4TXVVMlZqZEdsbmJ5QlNVMEVnUkc5dFlXbHVJRlpoYkdsa1lYUnBiMjRnVTJWamRYSmxJRk5sY25abGNpQkQKUVRBZUZ3MHlNakEyTURJd01EQXdNREJhRncweU16QTJNREl5TXpVNU5UbGFNQk14RVRBUEJnTlZCQU1NQ0NvdQpaV1JqTG1GbE1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBMkdPSmxqRkhGL0YvCmRDendBc2FiRlcxVEEvSC9EM2xQUGJFRVIwWWkxTzBLZGhralprUGZHV3RwalZlTE9jRUxGaCtFNDNGTE1UbC8KZnR1RWlZQVdNZnIrS3hnYmVqU2MySVN0a0dQZWpsVkk2Z01HZGVoeFptQWlXVjZ1STVXQ1djZEYrV1lmUlUxdgpHQnlsQWN6RFd1WXQ3bHlEQmVmekNNSVRnUUN1VHpKVVViSGYrV1NRZkRQak5STWJXS2NIYWppM1lTempaby9FCi9qMm5hdjZFeExMbWlnVVNMM1BoQjR0U1o4RlN4MWc4ZzFsTkNOV2ZnRzExcHFPaXAzdWNjSk9yRTc5ZzJpK2IKbUIzeTVxb2dZdW9jVjBBemh2MTdyTHJxV3cvaEZGQXJ2YXhWRkcwRkRwZnNHSld4azBXSXJTWS8xdEdPK1dsSwpMcGJOd3dSZlRRSURBUUFCbzRJQzlEQ0NBdkF3SHdZRFZSMGpCQmd3Rm9BVWpZeGV4RlN0aXVGMzZadjVtd1hoCnVBR05ZZUV3SFFZRFZSME9CQllFRkJuTGVSdGgwaVQwRXFBY0IyeVdFNUVRUWwxak1BNEdBMVVkRHdFQi93UUUKQXdJRm9EQU1CZ05WSFJNQkFmOEVBakFBTUIwR0ExVWRKUVFXTUJRR0NDc0dBUVVGQndNQkJnZ3JCZ0VGQlFjRApBakJKQmdOVkhTQUVRakJBTURRR0N5c0dBUVFCc2pFQkFnSUhNQ1V3SXdZSUt3WUJCUVVIQWdFV0YyaDBkSEJ6Ck9pOHZjMlZqZEdsbmJ5NWpiMjB2UTFCVE1BZ0dCbWVCREFFQ0FUQ0JoQVlJS3dZQkJRVUhBUUVFZURCMk1FOEcKQ0NzR0FRVUZCekFDaGtOb2RIUndPaTh2WTNKMExuTmxZM1JwWjI4dVkyOXRMMU5sWTNScFoyOVNVMEZFYjIxaAphVzVXWVd4cFpHRjBhVzl1VTJWamRYSmxVMlZ5ZG1WeVEwRXVZM0owTUNNR0NDc0dBUVVGQnpBQmhoZG9kSFJ3Ck9pOHZiMk56Y0M1elpXTjBhV2R2TG1OdmJUQWJCZ05WSFJFRUZEQVNnZ2dxTG1Wa1l5NWhaWUlHWldSakxtRmwKTUlJQmdBWUtLd1lCQkFIV2VRSUVBZ1NDQVhBRWdnRnNBV29BZHdDdDk3NzZmUDhReUl1ZFBad2VQaGhxdEdjcApYYyt4RENUS2hZWTA2OXlDaWdBQUFZRWozd2lBQUFBRUF3QklNRVlDSVFDYmUvK1V2SExTbHhUVWRWWnN6NytiCnVBaHpNaXJvWWtrRmR0eVZodDlmS0FJaEFLcUVqUTRhWTNHQVFabVpUenJxSEdMT2xRT0ZKVjZGSW9vNjRMOVkKNTR6ZEFIY0FlaktNVk5pM0xiWWc2ampnVWg3cGhCWndNaE9GVFR2U0s4RTZWNk5TNjFJQUFBR0JJOThJYWdBQQpCQU1BU0RCR0FpRUFzbkVES29FR01GdUpRdUlkTXZUNkoySUdJMk5hTnN2T3ZPY2c3VFZYcHFrQ0lRQ3dJNzZECjYvT0hrUllEVS9NMVFacWIxWXpvMk5ybXp0RzY0aDVEdng5enFnQjJBT2crME5vKzlRWTFNdWRYS0x5SmE4a0QKMDh2UkVXdnM2Mm5oZDMxdEJyMXVBQUFCZ1NQZkNEY0FBQVFEQUVjd1JRSWhBSkRUM1pxdTJvZCtkRDJRWkxNaAo4S3BPbEw3aGoveWozWStPMktMSFlKQjZBaUE2U1NEVURvaGR2dnhjWVBITjNmUVdwOHJKRHV3Q3NXVFVDMXRGCkRYZUNsREFOQmdrcWhraUc5dzBCQVFzRkFBT0NBUUVBeFVxNm04OHdhdUlwL1lab21SR0Q0TFR4bkNFUWlYT2gKbEVmUnN0ZGdjZXY3YWVsSVBUUXExbUErVE5TOFpjU3JlanpEQTR1UGgvenllUXNGVE8xWE84bFhyeG5WbmdsZApqeEhlSlJLVjMydncraHNuRkdTZ2NSUW4yeGt3MVBXaVA3ZnFzZXlLdDMxM1h4OU1IVmVJUUJlZWd0ejAxbkZXCjZLWXVRMnVmZmptOHJ3TkFqWVVlM1hYaW5vRTUyZzVKL1VGaFBPVlh3aGlQVVl5aEdTaENnQUYvZElXWHk1UFMKazRsUnhmbVdNb1JLY2RwR2dKL2Q4VlNkSjloNmZGdE11RzRGM1ZPUjVxV08weTJOWkFyTS9XUzVvUHBGcjVJeQp5Y2xWb0JsOWxXbVovR1NQTFRFaElNN2hCeHZQV00vQkdYaG5yendwYXZRZTF1UWZ5bjVrblE9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0t");
                //configBuilder.append("\n" + "config_tls_cert_chain = LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUdFekNDQS91Z0F3SUJBZ0lRZlZ0UkpyUjJ1aEhiZEJZTHZGTU5wekFOQmdrcWhraUc5dzBCQVF3RkFEQ0IKaURFTE1Ba0dBMVVFQmhNQ1ZWTXhFekFSQmdOVkJBZ1RDazVsZHlCS1pYSnpaWGt4RkRBU0JnTlZCQWNUQzBwbApjbk5sZVNCRGFYUjVNUjR3SEFZRFZRUUtFeFZVYUdVZ1ZWTkZVbFJTVlZOVUlFNWxkSGR2Y21zeExqQXNCZ05WCkJBTVRKVlZUUlZKVWNuVnpkQ0JTVTBFZ1EyVnlkR2xtYVdOaGRHbHZiaUJCZFhSb2IzSnBkSGt3SGhjTk1UZ3gKTVRBeU1EQXdNREF3V2hjTk16QXhNak14TWpNMU9UVTVXakNCanpFTE1Ba0dBMVVFQmhNQ1IwSXhHekFaQmdOVgpCQWdURWtkeVpXRjBaWElnVFdGdVkyaGxjM1JsY2pFUU1BNEdBMVVFQnhNSFUyRnNabTl5WkRFWU1CWUdBMVVFCkNoTVBVMlZqZEdsbmJ5Qk1hVzFwZEdWa01UY3dOUVlEVlFRREV5NVRaV04wYVdkdklGSlRRU0JFYjIxaGFXNGcKVm1Gc2FXUmhkR2x2YmlCVFpXTjFjbVVnVTJWeWRtVnlJRU5CTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQwpBUThBTUlJQkNnS0NBUUVBMW5NejF0YzhJTkFBMGhkRnVOWStCNkkveDBIdU1qREpzR3o5OUovTEVwZ1BMVCtOClRRRU1nZzhYZjJJdTZiaEllZnNXZzA2dDF6SWxrN2NIdjdsUVA2bE13MEFxNlRuLzJZSEtIeFl5UWRxQUpya2oKZW9jZ0h1UC9JSm84bFVSdmgzVUdrRUMwTXBNV0NSQUlJejdTM1ljUGIxMVJGR29LYWNWUEFYSnB6OU9UVEcwRQpvS01iZ242eG1ybnR4WjdGTjNpZm1nZzArMVl1V01RSkRnWmtXN3czM1BHZktHaW9WckNTbzF5ZnU0aVlDQnNrCkhhc3doYTZ2c0M2ZWVwM0J3RUljNGdMdzZ1QkswdStRRHJUQlFCYndiNFZDU21UM3BEQ2cvcjh1b3lkYWpvdFkKdUszREdSZUVZKzF2VnYyRHkyQTB4SFMrNXAzYjRlVGx5Z3hmRlFJREFRQUJvNElCYmpDQ0FXb3dId1lEVlIwagpCQmd3Rm9BVVUzbS9XcW9yU3M5VWdPSFltOENkOHJJRFpzc3dIUVlEVlIwT0JCWUVGSTJNWHNSVXJZcmhkK21iCitac0Y0YmdCaldIaE1BNEdBMVVkRHdFQi93UUVBd0lCaGpBU0JnTlZIUk1CQWY4RUNEQUdBUUgvQWdFQU1CMEcKQTFVZEpRUVdNQlFHQ0NzR0FRVUZCd01CQmdnckJnRUZCUWNEQWpBYkJnTlZIU0FFRkRBU01BWUdCRlVkSUFBdwpDQVlHWjRFTUFRSUJNRkFHQTFVZEh3UkpNRWN3UmFCRG9FR0dQMmgwZEhBNkx5OWpjbXd1ZFhObGNuUnlkWE4wCkxtTnZiUzlWVTBWU1ZISjFjM1JTVTBGRFpYSjBhV1pwWTJGMGFXOXVRWFYwYUc5eWFYUjVMbU55YkRCMkJnZ3IKQmdFRkJRY0JBUVJxTUdnd1B3WUlLd1lCQlFVSE1BS0dNMmgwZEhBNkx5OWpjblF1ZFhObGNuUnlkWE4wTG1OdgpiUzlWVTBWU1ZISjFjM1JTVTBGQlpHUlVjblZ6ZEVOQkxtTnlkREFsQmdnckJnRUZCUWN3QVlZWmFIUjBjRG92CkwyOWpjM0F1ZFhObGNuUnlkWE4wTG1OdmJUQU5CZ2txaGtpRzl3MEJBUXdGQUFPQ0FnRUFNcjlodlE1SXcwL0gKdWtkTitKeDRHUUhjRXgyQWIvekRjTFJTbWpFem1sZFMrekdlYTZUdlZLcUpqVUFYYVBnUkVIelN5ckh4VlliSAo3ck0ya1liMk9WRy9ScjhQb0xxMDkzNUp4Q28yRjU3a2FEbDZyNVJPVm0reWV6dS9Db2E5emNWM0hBTzRPTEdpCkgxOSsyNHJjUmtpMmFBclBzclcwNGpUa1o2azRaZ2xlMHJqOG5TZzZGMEFud25KT0tmMGhQSHpQRS91V0xNVXgKUlAwVDdkV2JxV2xvZDN6dTRmK2srVFk0Q0ZNNW9vUTBuQm56dmc2czFTUTM2eU9vZU5EVDUrK1NSMlJpT1NMdgp4dmNSdmlLRnhtWkVKQ2FPRURLTnlKT3VCNTZEUGkvWitmVkdqbU8rd2VhMDNLYk5JYWlHQ3BYWkxvVW1HdjM4CnNiWlhRbTJWMFRQMk9SUUdna0U0OVk5WTNJQmJwTlY5bFhqOXA1di8vY1dvYWFzbTU2ZWtCWWRicWJlNG95QUwKbDZsRmhkMnppK1dKTjQ0cERmd0dGL1k0UUE1QzVCSUcrM3Z6eGhGb1l0L2ptUFFUMkJWUGk3RnAyUkJndkdRcQo2akczNUxXak9oU2JKdU1MZS8wQ2pyYVp3VGlYV1RiMnFIU2loclplNjhaazZzK2dvL2x1bnJvdEViYUdtQWhZCkxjbXNKV1R5WG5XME9NR3VmMXBHZytwUnlyYnhtUkUxYTZWcWU4WUFzT2Y0dm1TeXJjakM4YXpqVWVxa2srQjUKeU9HQlFNa0tXK0VTUE1GZ0t1T1h3SWxDeXBUUFJwZ1NhYnVZME1MVERYSkxSMjdsazhReUtHT0hRK1N3TWo0SwowMHUvSTVzVUtVRXJtZ1Fma3kzeHh6bElQSzFhRW44PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCi0tLS0tQkVHSU4gQ0VSVElGSUNBVEUtLS0tLQpNSUlGZ1RDQ0JHbWdBd0lCQWdJUU9YSkVPdmtpdDFIWDAyd1EzVEUxbFRBTkJna3Foa2lHOXcwQkFRd0ZBREI3Ck1Rc3dDUVlEVlFRR0V3SkhRakViTUJrR0ExVUVDQXdTUjNKbFlYUmxjaUJOWVc1amFHVnpkR1Z5TVJBd0RnWUQKVlFRSERBZFRZV3htYjNKa01Sb3dHQVlEVlFRS0RCRkRiMjF2Wkc4Z1EwRWdUR2x0YVhSbFpERWhNQjhHQTFVRQpBd3dZUVVGQklFTmxjblJwWm1sallYUmxJRk5sY25acFkyVnpNQjRYRFRFNU1ETXhNakF3TURBd01Gb1hEVEk0Ck1USXpNVEl6TlRrMU9Wb3dnWWd4Q3pBSkJnTlZCQVlUQWxWVE1STXdFUVlEVlFRSUV3cE9aWGNnU21WeWMyVjUKTVJRd0VnWURWUVFIRXd0S1pYSnpaWGtnUTJsMGVURWVNQndHQTFVRUNoTVZWR2hsSUZWVFJWSlVVbFZUVkNCTwpaWFIzYjNKck1TNHdMQVlEVlFRREV5VlZVMFZTVkhKMWMzUWdVbE5CSUVObGNuUnBabWxqWVhScGIyNGdRWFYwCmFHOXlhWFI1TUlJQ0lqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FnOEFNSUlDQ2dLQ0FnRUFnQkpsRnpZT3c5c0kKczlDc1Z3MTI3YzBuMDB5dFVJTmg0cW9nVFFrdFpBbmN6b21mekQycDdQYlB3ZHp4MDdIV2V6Y29FU3RIMmpuRwp2RG9adEYrbXZYMmRvMk5DdG5ieXFUc3JrZmppYjlEc0ZpQ1FDVDdpNkhUSkdMU1IxR0prMjMrakJ2R0lHR3FRCklqeTgvaFB3aHhSNzl1UWZqdFRrVWNZUlowWUlVY3VHRkZRL3ZEUCtmbXljL3hhZEdMMVJqaldtcDJiSWNtZmIKSVdheDFKdDRBOEJRT3VqTThOeThua3orcndXV05SOVhXcmYvenZrOXR5eTI5bFRkeU9jU09rMnVUSXEzWEpxMAp0eUE5eW44aU5LNStPMmhtQVVUbkFVNUdVNXN6WVBlVXZsTTNrSE5EOHpMRFUrL2JxdjUwVG1uSGE0eGdrOTdFCnh3emY0VEt1ekpNN1VYaVZaNHZ1UFZiK0ROQnBEeHNQOHlVbWF6TnQ5MjVIK25ORDVYNE9wV2F4S1h3eWhHTlYKaWNRTndaTlVNQmtUck5OOU42ZnJYVHBzTlZ6YlFkY1MycWxKQzkvWWdJb0prMktPdFdiUEpZak5oTGl4UDZRNQpEOWtDbnVzU1RKVjg4MnNGcVY0V2c4eTRaK0xvRTUzTVc0TFRUTFB0Vy8vZTVYT3NJenN0QUw4MVZYUUpTZGhKCldCcC9ramJtVVpJTzh5WjlIRTBYdk1uc1F5YlF2MEZmUUtsRVJQU1o1MWVIbmxBZlYxU29QdjEwWXkreFVHVUoKNWxoQ0xrTWFUTFR3SlVkWitnUWVrOVFtUmtwUWdiTGV2bmkzL0djVjRjbFhoQjRQWTlicFlycldYMVV1Nmx6RwpLQWdFSlRtNERpdXA4a3lYSEFjL0RWTDE3ZTh2Z2c4Q0F3RUFBYU9COGpDQjd6QWZCZ05WSFNNRUdEQVdnQlNnCkVRb2pQcGJ4Qit6aXJ5bnZncVYvMERDa3REQWRCZ05WSFE0RUZnUVVVM20vV3FvclNzOVVnT0hZbThDZDhySUQKWnNzd0RnWURWUjBQQVFIL0JBUURBZ0dHTUE4R0ExVWRFd0VCL3dRRk1BTUJBZjh3RVFZRFZSMGdCQW93Q0RBRwpCZ1JWSFNBQU1FTUdBMVVkSHdROE1Eb3dPS0Eyb0RTR01taDBkSEE2THk5amNtd3VZMjl0YjJSdlkyRXVZMjl0CkwwRkJRVU5sY25ScFptbGpZWFJsVTJWeWRtbGpaWE11WTNKc01EUUdDQ3NHQVFVRkJ3RUJCQ2d3SmpBa0JnZ3IKQmdFRkJRY3dBWVlZYUhSMGNEb3ZMMjlqYzNBdVkyOXRiMlJ2WTJFdVkyOXRNQTBHQ1NxR1NJYjNEUUVCREFVQQpBNElCQVFBWWgxSGNkQ0U5bklyZ0o3Y3owQzdNN1BEbXkxNFIzaUp2bTNXT25uTCs1TmIrcWgrY2xpM3ZBMHArCnJ2U05iM0k4UXp2QVArdTQzMXlxcWNhdTh2elk3cU43US9hR05ud1U0TTMwOXovKzNyaTBpdkNSbHY3OVEyUisKL2N6U0FhRjlmZmdaR2NsQ0t4Ty9XSXU2cEtKbUJIYUlrVTRNaVJUT29rM0pNck82NkJRYXZISHhXL0JCQzVnQQpDaUlERU9VTXNmbk5ramNaN1R2eDVEcTIrVVVUSm5XdnU2cnZQM3QzTzlMRUFwRTlHUURURjF3NTJ6OTdHQTFGCnpaT0ZsaTlkMzFrV1R6OVJ2ZFZGR0QvdFNvN29CbUYwSXhhMURWQnpKMFJIZnhCZGlTcHJoVEVVeE9pcGFreUEKdkdwNHo3aC9qblp5bVF5ZC90ZVJDQmFobzErVgotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCi0tLS0tQkVHSU4gQ0VSVElGSUNBVEUtLS0tLQpNSUlFTWpDQ0F4cWdBd0lCQWdJQkFUQU5CZ2txaGtpRzl3MEJBUVVGQURCN01Rc3dDUVlEVlFRR0V3SkhRakViCk1Ca0dBMVVFQ0F3U1IzSmxZWFJsY2lCTllXNWphR1Z6ZEdWeU1SQXdEZ1lEVlFRSERBZFRZV3htYjNKa01Sb3cKR0FZRFZRUUtEQkZEYjIxdlpHOGdRMEVnVEdsdGFYUmxaREVoTUI4R0ExVUVBd3dZUVVGQklFTmxjblJwWm1sagpZWFJsSUZObGNuWnBZMlZ6TUI0WERUQTBNREV3TVRBd01EQXdNRm9YRFRJNE1USXpNVEl6TlRrMU9Wb3dlekVMCk1Ba0dBMVVFQmhNQ1IwSXhHekFaQmdOVkJBZ01Fa2R5WldGMFpYSWdUV0Z1WTJobGMzUmxjakVRTUE0R0ExVUUKQnd3SFUyRnNabTl5WkRFYU1CZ0dBMVVFQ2d3UlEyOXRiMlJ2SUVOQklFeHBiV2wwWldReElUQWZCZ05WQkFNTQpHRUZCUVNCRFpYSjBhV1pwWTJGMFpTQlRaWEoyYVdObGN6Q0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQCkFEQ0NBUW9DZ2dFQkFMNUFuZlJ1NGVwMmh4eE5SVVNPdmtiSWd3YWR3U3IrR0IrTzVBTDY4NnRkVUlvV01RdWEKQnRERmNDTE5TUzFVWTh5MmJtaEdDMVBxeTB3a3dMeHlUdXJ4RmE3MFZKb1NDc042c2pOZzR0cUpWZk1pV1BQZQozTS92ZzRhaWpKUlBuMmp5bUpCR2hDZkhkci9qekRVc2kxNEhaR1dDd0Vpd3FKSDVZWjkySUZDb2tjZG10ZXQ0CllnTlc4SW9hRStveG94NmdtZjA0OXZZbk1saHZCL1ZydVBzVUs2KzNxc3pXWTE5empOb0ZtYWc0cU1zWGVEWlIKck9tZTlIZzZqYzhQMlVMaW1BeXJMNThPQWQ3dm41bEo4UzNmckhSTkc1aTFSOFhsS2RINWtCakhZcHkrZzhjbQplejZLSmNmQTNaM21OV2dRSUoyUDJON1N3NFNjRFY3b0w4a0NBd0VBQWFPQndEQ0J2VEFkQmdOVkhRNEVGZ1FVCm9CRUtJejZXOFFmczRxOHA3NEtsZjlBd3BMUXdEZ1lEVlIwUEFRSC9CQVFEQWdFR01BOEdBMVVkRXdFQi93UUYKTUFNQkFmOHdld1lEVlIwZkJIUXdjakE0b0RhZ05JWXlhSFIwY0RvdkwyTnliQzVqYjIxdlpHOWpZUzVqYjIwdgpRVUZCUTJWeWRHbG1hV05oZEdWVFpYSjJhV05sY3k1amNtd3dOcUEwb0RLR01HaDBkSEE2THk5amNtd3VZMjl0CmIyUnZMbTVsZEM5QlFVRkRaWEowYVdacFkyRjBaVk5sY25acFkyVnpMbU55YkRBTkJna3Foa2lHOXcwQkFRVUYKQUFPQ0FRRUFDRmI4QXZDYjZQK2srdFo3eGtTQXprL0V4ZllBV015bXRyd1VTV2dFZHVqbTdsM3NBZzlnMW8xUQpHRThtVGdIajVyQ2w3cis4ZEZSQnYvMzhFcmpIVDFyMGlXQUZmMkMzQlVyejl2SEN2OFM1ZElhMkxYMXJ6Tkx6ClJ0MHZ4dUJxdzhNMEF5eDlsdDFhd2c2bkNwbkJCWXVyREMvelhEclBiRGRWQ1lmZVUwQnNXTy84dHF0bGJnVDIKRzl3ODRGb1Z4cDdaOFZsSU1DRmxBMnpzNlNGejdKc0RvZUEzcmFBVkdJLzZ1Z0xPcHl5cEVCTXMxT1VJSnFzaQpsMkQ0a0Y1MDFLS2FVNzN5cVdqZ29tN0MxMnl4b3crZXYrdG81MWJ5cnZMakt6ZzZDWUcxYTRYWHZpM3RQeHEzCnNtUGk5V0lzZ3RScUFFRlE4VG1EbjVYcE5wYVliZz09Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0=");


                //org
//                if (!TextUtils.isEmpty(AppController.VG_URL)) {
//                configBuilder.append("\n" + "vg_url =" + AppController.VG_URL);
//                //configBuilder.append("\n" + "vg_url =" + "https://101.53.158.186/VGPreProd/EIDALAB/ValidationGateway");
//                //configBuilder.append("\n" + "config_url =" + "https://101.53.158.186/configdev/");
//                }

                // temp
                configBuilder.append("\n" + "vg_url =" + "https://appshield.digitaltrusttech.com/VGProd/ZICALAB/ValidationGateway/Service");
                //configBuilder.append("\n" + "config_url =" + "https://101.53.158.186/configdev/");


                String pluginDirectorPath = context.getApplicationInfo().nativeLibraryDir + "/";
                configBuilder.append("\n" + "plugin_directory_path =" + pluginDirectorPath);
                Logger.d("configBuilder ::" + configBuilder.toString());
                toolkit = new Toolkit(true, configBuilder.toString(), context);
                Logger.d("Toolkit init success ");
                CryptoUtils.setPublickey(toolkit.getDataProtectionKey().getPublicKey());
                //this will give you the current version of toolkit.
                Logger.d("Toolkit version is " + toolkit.getToolkitVerison());
                return true;
            } catch (ToolkitException e) {
                Logger.e("Exception occurred in initializing " + e.getLocalizedMessage());
                throw e;
            }//catch()..
        }
        return true;
    }

    public static Toolkit getToolkit() throws ToolkitException {
        return toolkit;
    }

    public static Toolkit getToolkitObject() throws ToolkitException {
        if (toolkit == null) {
            throw new ToolkitException("Toolkit is not initialized.");
        }
        return toolkit;
    }


    public static CardReader initConnection() throws ToolkitException {

        if (toolkit == null) {
            throw new ToolkitException(" Please initialize Toolkit first");
        }

        if (cardReader != null) {
            if (cardReader.isConnected()) {
                cardReader.disconnect();
            }
        }

        if (cardReader == null || !cardReader.isConnected()) {
            try {
                cardReader = toolkit.getReaderWithEmiratesID();
                //cardReader = toolkit.listReaders();
                // Get the first reader.

                Logger.d("list reader successful" + cardReader.getName());
                cardReader.connect();
                Logger.d("Connection Success full  " + cardReader.isConnected());

            } catch (ToolkitException e) {
                Logger.e("ToolkitException::Connection failed>" + e.getMessage());
                cardReader = null;
                throw e;
            }//catch()
            catch (Exception e) {
                Logger.e("Exception::Connection failed with handle" + e.getMessage());
                cardReader = null;
                throw e;
            }//catch()
        }//if()
        else {
            Logger.d("connection exists " + cardReader.isConnected());
        }
        //connection is already exits return the same.
        return cardReader;
    }//initConnection()...

    public static void setNFCParams(String cardNumber, String dob, String expiryDate) throws ToolkitException {
        if (cardReader == null || !cardReader.isConnected()) {
            return;
        }
        cardReader.setNfcAuthenticationParameters(cardNumber, dob, expiryDate);

    }


    public static CardReader getConnection() throws ToolkitException {
        if (toolkit == null) {
            throw new ToolkitException("Toolkit is not initialized.");
        }
        if (cardReader == null || !cardReader.isConnected()) {
            throw new ToolkitException("Card not connected");
        }
        return cardReader;
    }//getConnection() ..

    public static CardReader initConnection(Tag tag) throws ToolkitException {
        if (toolkit == null) {
            if (toolkit == null) {
                throw new ToolkitException(" Please initialize Toolkit first");
            }
        }
        try {
            if (cardReader != null && cardReader.isConnected()) {
                closeConnection();
            }
            Logger.d("Creating a new connection successfully initialized");
            toolkit.setNfcMode(tag);
//          discover all the readers connected to the system
            CardReader[] cardReaders = toolkit.listReaders();


            if (cardReaders == null || cardReaders.length == 0) {
                Logger.e("No reader are founded");
                return cardReader;
            }//if()
            Logger.d("list reader successful" + cardReaders.length);

            cardReader = new CardReader(cardReaders[0].getName());
            //Get the first reader.

            Logger.d("list reader successful" + cardReader.getName());


            cardReader.connect();
            Logger.d("Connection Success full  " + cardReader.isConnected());

        } catch (ToolkitException e) {
            Logger.e("ToolkitException::Connection failed>" + e.getMessage());
            cardReader = null;
            throw e;
        }//catch()
        catch (Exception e) {
            Logger.e("Exception::Connection failed with handle" + e.getMessage());
            cardReader = null;
            throw e;
        }//catch()
        //connection is already exits return the same.
        return cardReader;
    }//initConnection()...

    public static void closeConnection() {
        Logger.d("Disconnecting ");
        if (null == cardReader) {
            return;
        }//if()
        try {
            if (cardReader.isConnected()) {

                cardReader.disconnect();
                Logger.d("Reader Disconnected Status ");
            }//if()
        } catch (ToolkitException e) {
            Logger.e("Failed to disconnect" + e.getMessage() + ",,," + e.getCode());
        }//
        finally {
            Logger.d("connection Reset");
        }//finally
    }//closeConnection()....

    public static void cleanup() {
        if (toolkit != null) {
            try {
                closeConnection();
                toolkit.cleanup();
            } catch (ToolkitException e) {
                Logger.e("Failed to disconnect" + e.getMessage() + ",,," + e.getCode());
            } finally {
                toolkit = null;
            }//finally
        }//if()
    }//cleanup()

    private static String readFileFromPath(String path) {

        //read the file
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }//if()

        //create stream to read the file
        FileInputStream in = null;
        String fileContents = null;
        try {
            in = new FileInputStream(file);
            byte[] contents = new byte[in.available()];
            in.read(contents);
            fileContents = new String(contents);
            Logger.d("File read completed successfully.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logger.e("File read failed . " + e.getLocalizedMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e("File read failed . " + e.getLocalizedMessage());
        }//catch
        finally {
            if (in != null) {

                //close the stream
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }//catch()
            }//if(()
        }//finally
        return fileContents;
    }//readFileFromPath
}//end-of-class
