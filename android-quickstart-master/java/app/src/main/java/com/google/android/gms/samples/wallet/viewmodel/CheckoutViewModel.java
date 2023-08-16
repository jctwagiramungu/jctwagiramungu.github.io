package com.google.android.gms.samples.wallet.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.pay.Pay;
import com.google.android.gms.pay.PayApiAvailabilityStatus;
import com.google.android.gms.pay.PayClient;
import com.google.android.gms.samples.wallet.util.PaymentsUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;

import org.json.JSONObject;

import java.util.Date;

public class CheckoutViewModel extends AndroidViewModel {

    // A client for interacting with the Google Pay API.
    private final PaymentsClient paymentsClient;

    // A client to interact with the Google Wallet API
    private final PayClient walletClient;

    // LiveData with the result of whether the user can pay using Google Pay
    private final MutableLiveData<Boolean> _canUseGooglePay = new MutableLiveData<>();

    // LiveData with the result of whether the user can save passes with Google Wallet
    private final MutableLiveData<Boolean> _canAddPasses = new MutableLiveData<>();

    public CheckoutViewModel(@NonNull Application application) {
        super(application);
        paymentsClient = PaymentsUtil.createPaymentsClient(application);
        walletClient = Pay.getClient(application);

        fetchCanUseGooglePay();
        fetchCanAddPassesToGoogleWallet();
    }

    public final LiveData<Boolean> canUseGooglePay = _canUseGooglePay;
    public final LiveData<Boolean> canAddPasses = _canAddPasses;

    /**
     * Determine the user's ability to pay with a payment method supported by your app and display
     * a Google Pay payment button.
     */
    private void fetchCanUseGooglePay() {
        final JSONObject isReadyToPayJson = PaymentsUtil.getIsReadyToPayRequest();
        if (isReadyToPayJson == null) {
            _canUseGooglePay.setValue(false);
            return;
        }

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString());
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(
                completedTask -> {
                    if (completedTask.isSuccessful()) {
                        _canUseGooglePay.setValue(completedTask.getResult());
                    } else {
                        Log.w("isReadyToPay failed", completedTask.getException());
                        _canUseGooglePay.setValue(false);
                    }
                });
    }

    /**
     * Creates a Task that starts the payment process with the transaction details included.
     *
     * @param priceCents the price to show on the payment sheet.
     * @return a Task with the payment information.
     * )
     */
    public Task<PaymentData> getLoadPaymentDataTask(final long priceCents) {
        JSONObject paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(priceCents);
        if (paymentDataRequestJson == null) {
            return null;
        }

        PaymentDataRequest request =
                PaymentDataRequest.fromJson(paymentDataRequestJson.toString());
        return paymentsClient.loadPaymentData(request);
    }

    /**
     * Determine whether the API to save passes to Google Pay is available on the device.
     */
    private void fetchCanAddPassesToGoogleWallet() {
        walletClient
            .getPayApiAvailabilityStatus(PayClient.RequestType.SAVE_PASSES)
            .addOnSuccessListener(
                status -> _canAddPasses.setValue(status == PayApiAvailabilityStatus.AVAILABLE))
            // If the API is not available, we recommend to either:
            // 1) Hide the save button
            // 2) Fall back to a different Save Passes integration (e.g. JWT link)
            // Note that a user might become eligible in the future.

            // Google Play Services is too old. API availability can't be verified.
            .addOnFailureListener(exception -> _canAddPasses.setValue(false));
    }

    /**
     * Exposes the `savePassesJwt` method in the wallet client
     */
    public void savePassesJwt(String jwtString, Activity activity, int requestCode) {
        walletClient.savePassesJwt(jwtString, activity, requestCode);
    }

    /**
     * Exposes the `savePasses` method in the wallet client
     */
    public void savePasses(String objectString, Activity activity, int requestCode) {
        walletClient.savePasses(objectString, activity, requestCode);
    }

    // Test generic object used to be created against the API
    // See https://developers.google.com/wallet/tickets/boarding-passes/web#json_web_token_jwt for more details
    public final String genericObjectJwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJnb29nbGUiLCJwYXlsb2FkIjp7ImdlbmVyaWNPYmplY3RzIjpbeyJpZCI6IjMzODgwMDAwMDAwMjIwOTUxNzcuZjUyZDRhZjYtMjQxMS00ZDU5LWFlNDktNzg2ZDY3N2FkOTJiIn1dfSwiaXNzIjoid2FsbGV0LWxhYi10b29sc0BhcHBzcG90LmdzZXJ2aWNlYWNjb3VudC5jb20iLCJ0eXAiOiJzYXZldG93YWxsZXQiLCJpYXQiOjE2NTA1MzI2MjN9.ZURFHaSiVe3DfgXghYKBrkPhnQy21wMR9vNp84azBSjJxENxbRBjqh3F1D9agKLOhrrflNtIicShLkH4LrFOYdnP6bvHm6IMFjqpUur0JK17ZQ3KUwQpejCgzuH4u7VJOP_LcBEnRtzZm0PyIvL3j5-eMRyRAo5Z3thGOsKjqCPotCAk4Z622XHPq5iMNVTvcQJaBVhmpmjRLGJs7qRp87sLIpYOYOkK8BD7OxLmBw9geqDJX-Y1zwxmQbzNjd9z2fuwXX66zMm7pn6GAEBmJiqollFBussu-QFEopml51_5nf4JQgSdXmlfPrVrwa6zjksctIXmJSiVpxL7awKN2w";

    public final String newObjectJson = "{\n" +
            "   \"iss\":\"jeanclaude.twagiramungu@gmail.com\",\n" +
            "   \"aud\":\"google\",\n" +
            "   \"typ\":\"savetowallet\",\n" +
            "   \"iat\":\"" + new Date().getTime() / 1000L + "\",\n" +
            "   \"origins\":[\n" +
            "      \n" +
            "   ],\n" +
            "   \"payload\":{\n" +
            "      \"genericObjects\":[\n" +
            "{\"id\": \"3388000000022260574.a4aada3d-59f3-4543-9820-9375c10f6515\",\n" +
            "        \"classTemplateInfo\": {\n" +
            "          \"cardTemplateOverride\": {\n" +
            "            \"cardRowTemplateInfos\": [\n" +
            "              {\n" +
            "                \"twoItems\": {\n" +
            "                  \"startItem\": {\n" +
            "                    \"firstValue\": {\n" +
            "                      \"fields\": [\n" +
            "                        {\n" +
            "                          \"fieldPath\": \"object.textModulesData['points']\",\n" +
            "                        },\n" +
            "                      ],\n" +
            "                    },\n" +
            "                  },\n" +
            "                  \"endItem\": {\n" +
            "                    \"firstValue\": {\n" +
            "                      \"fields\": [\n" +
            "                        {\n" +
            "                          \"fieldPath\": \"object.textModulesData['contacts']\",\n" +
            "                        },\n" +
            "                      ],\n" +
            "                    },\n" +
            "                  },\n" +
            "                },\n" +
            "              },\n" +
            "            ],\n" +
            "          },\n" +
            "          \"detailsTemplateOverride\": {\n" +
            "            \"detailsItemInfos\": [\n" +
            "              {\n" +
            "                \"item\": {\n" +
            "                  \"firstValue\": {\n" +
            "                    \"fields\": [\n" +
            "                      {\n" +
            "                        \"fieldPath\": \"class.imageModulesData['event_banner']\",\n" +
            "                      },\n" +
            "                    ],\n" +
            "                  },\n" +
            "                },\n" +
            "              },\n" +
            "              {\n" +
            "                \"item\": {\n" +
            "                  \"firstValue\": {\n" +
            "                    \"fields\": [\n" +
            "                      {\n" +
            "                        \"fieldPath\": \"class.textModulesData['game_overview']\",\n" +
            "                      },\n" +
            "                    ],\n" +
            "                  },\n" +
            "                },\n" +
            "              },\n" +
            "              {\n" +
            "                \"item\": {\n" +
            "                  \"firstValue\": {\n" +
            "                    \"fields\": [\n" +
            "                      {\n" +
            "                        \"fieldPath\": \"class.linksModuleData.uris['official_site']\",\n" +
            "                      },\n" +
            "                    ],\n" +
            "                  },\n" +
            "                },\n" +
            "              },\n" +
            "            ],\n" +
            "          },\n" +
            "        },\n" +
            "        \"imageModulesData\": [\n" +
            "          {\n" +
            "            \"mainImage\": {\n" +
            "              \"kind\": \"walletobjects#image\",\n" +
            "              \"sourceUri\": {\n" +
            "                \"uri\": \"https://storage.googleapis.com/wallet-lab-tools-codelab-artifacts-public/google-io-2021-card.png\",\n" +
            "              },\n" +
            "              \"contentDescription\": {\n" +
            "                \"kind\": \"walletobjects#localizedString\",\n" +
            "                \"defaultValue\": {\n" +
            "                  \"kind\": \"walletobjects#translatedString\",\n" +
            "                  \"language\": \"en\",\n" +
            "                  \"value\": \"Google I/O 2022 Banner\",\n" +
            "                },\n" +
            "              },\n" +
            "            },\n" +
            "            \"id\": \"event_banner\",\n" +
            "          },\n" +
            "        ],\n" +
            "        \"textModulesData\": [\n" +
            "          {\n" +
            "            \"header\": \"Gather points meeting new people at Google I/O\",\n" +
            "            \"body\": \"Join the game and accumulate points in this badge by meeting other attendees in the event.\",\n" +
            "            \"id\": \"game_overview\",\n" +
            "          },\n" +
            "        ],\n" +
            "        \"linksModuleData\": {\n" +
            "          \"uris\": [\n" +
            "            {\n" +
            "              \"uri\": \"https://io.google/2022/\",\n" +
            "              \"description\": \"Official I/O '22 Site\",\n" +
            "              \"id\": \"official_site\",\n" +
            "            },\n" +
            "          ],\n" +
            "        },\n" +
            "      }\n" +
            "      ]\n" +
            "   }\n" +
            "}";

}