package org.nearbyshops.whitelabelapp.EditDataScreens.EditBannerImage;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.squareup.picasso.Picasso;

import org.nearbyshops.whitelabelapp.API.BannerImageService;
import org.nearbyshops.whitelabelapp.DaggerComponentBuilder;
import org.nearbyshops.whitelabelapp.Model.Image;
import org.nearbyshops.whitelabelapp.Preferences.PrefGeneral;
import org.nearbyshops.whitelabelapp.Preferences.PrefLogin;
import org.nearbyshops.whitelabelapp.R;
import org.nearbyshops.whitelabelapp.Utility.UtilityFunctions;
import org.nearbyshops.whitelabelapp.UtilityScreens.BannerSlider.Model.BannerImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;


public class EditBannerImageFragment extends Fragment {

    public static int PICK_IMAGE_REQUEST = 21;
    // Upload the image after picked up
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 56;


    public static final String BANNER_ID_INTENT_KEY = "banner_id_intent_key";



    @Inject
    BannerImageService bannerImageService;


    // flag for knowing whether the image is changed or not
    boolean isImageChanged = false;
    boolean isImageRemoved = false;


    // bind views
    @BindView(R.id.uploadImage) ImageView resultView;


    @BindView(R.id.imageID) EditText imageID;
    @BindView(R.id.imageOrder) EditText imageOrder;
    @BindView(R.id.itemIDtoOpen) EditText itemIDToOpen;
    @BindView(R.id.shopIDToOpen) EditText shopIDToOpen;
    @BindView(R.id.show_in_front_screen) CheckBox showInFrontScreen;
    @BindView(R.id.show_in_item_screen) CheckBox showInItemScreen;
    @BindView(R.id.show_in_shop_screen) CheckBox showInShopScreen;

    @BindView(R.id.saveButton) TextView buttonUpdateItem;
    @BindView(R.id.progress_bar) ProgressBar progressBar;


    public static final String EDIT_MODE_INTENT_KEY = "edit_mode";

    public static final int MODE_UPDATE = 52;
    public static final int MODE_ADD = 51;

    int current_mode = MODE_ADD;

    boolean isDestroyed = false;


    BannerImage bannerImage;



    public EditBannerImageFragment() {

        DaggerComponentBuilder.getInstance()
                .getNetComponent().Inject(this);
    }




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.content_edit_banner_image_fragment, container, false);
        ButterKnife.bind(this,rootView);



        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        if(savedInstanceState==null)
        {

            current_mode = getActivity().getIntent().getIntExtra(EDIT_MODE_INTENT_KEY,MODE_ADD);

            if(current_mode == MODE_UPDATE)
            {
                getBannerImageDetails();

            }
            else if (current_mode == MODE_ADD)
            {


            }

        }




        updateIDFieldVisibility();

        return rootView;
    }



    void updateIDFieldVisibility()
    {

        if(current_mode==MODE_ADD)
        {
            buttonUpdateItem.setText("Add");
            imageID.setVisibility(View.GONE);


            if(((AppCompatActivity)getActivity()).getSupportActionBar()!=null)
            {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Add Banner");
            }
        }
        else if(current_mode== MODE_UPDATE)
        {
            imageID.setVisibility(View.VISIBLE);
            buttonUpdateItem.setText("Save");


            if(((AppCompatActivity)getActivity()).getSupportActionBar()!=null)
            {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Edit Banner");
            }
        }
    }





    private void getBannerImageDetails() {

        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage(getString(R.string.loading_message));
        pd.show();



        Call<BannerImage> call = bannerImageService.getBannerImageDetails(
                getActivity().getIntent().getIntExtra(BANNER_ID_INTENT_KEY,0)
        );


        call.enqueue(new Callback<BannerImage>() {
            @Override
            public void onResponse(Call<BannerImage> call, Response<BannerImage> response) {
                pd.dismiss();

                if (response.code() == 200) {
                    bannerImage = response.body();
                    bindDataToViews();

                } else {
                    showToastMessage("Failed : Code : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BannerImage> call, Throwable t) {
                showToastMessage("Failed !");
            }
        });

    }







    public static final String TAG_LOG = "TAG_LOG";

    void showLogMessage(String message)
    {
        Log.i(TAG_LOG,message);
        System.out.println(message);
    }




    void loadImage(String filename) {

//        String iamgepath = UtilityGeneral.getServiceURL(getContext()) + "/api/v1/ItemImage/five_hundred_" + imagePath + ".jpg";


    }




    @OnClick(R.id.saveButton)
    public void UpdateButtonClick()
    {

        if(!validateData())
        {
//            showToastMessage("Please correct form data before save !");
            return;
        }

        if(current_mode == MODE_ADD)
        {
            bannerImage = new BannerImage();
            addAccount();
        }
        else if(current_mode == MODE_UPDATE)
        {
            update();
        }
    }




    boolean validateData()
    {
        boolean isValid = true;
//
//        if(captionTitle.getText().toString().length()==0)
//        {
//            captionTitle.setError("Image name cannot be empty !");
//            captionTitle.requestFocus();
//            isValid= false;
//        }



        return isValid;
    }




    void addAccount()
    {
        if(isImageChanged)
        {
            if(!isImageRemoved)
            {
                // upload image with add
                uploadPickedImage(false);
            }


            // reset the flags
            isImageChanged = false;
            isImageRemoved = false;

        }
        else
        {
            // post request
            retrofitPOSTRequest();
        }

    }






    void update()
    {

        if(isImageChanged)
        {


            // delete previous Image from the Server
            deleteImage(bannerImage.getImageFilename());

            /*ImageCalls.getInstance()
                    .deleteImage(
                            itemForEdit.getItemImageURL(),
                            new DeleteImageCallback()
                    );*/


            if(isImageRemoved)
            {

                bannerImage.setImageFilename(null);
                retrofitPUTRequest();

            }else
            {

                uploadPickedImage(true);
//                retrofitPUTRequest();
            }


            // resetting the flag in order to ensure that future updates do not upload the same image again to the server
            isImageChanged = false;
            isImageRemoved = false;

        }else {

            retrofitPUTRequest();
        }
    }






    void bindDataToViews()
    {
        if(bannerImage !=null) {

            imageID.setText(String.valueOf(bannerImage.getBannerImageID()));

            shopIDToOpen.setText(String.valueOf(bannerImage.getShopIdToOpen()));
            itemIDToOpen.setText(String.valueOf(bannerImage.getItemIDToOpen()));

            imageOrder.setText(String.valueOf(bannerImage.getSortOrder()));

            showInFrontScreen.setChecked(bannerImage.isShowInFrontScreen());
            showInItemScreen.setChecked(bannerImage.isShowInItemsScreen());
            showInShopScreen.setChecked(bannerImage.isShowInShopHome());



//            String imagePath =
//                    PrefGeneral.getServiceURL(getActivity()) + "/api/v1/BannerImages/Image/five_hundred_"
//                            + bannerImage.getImageFilename() + ".jpg";

            String imagePath = PrefGeneral.getServerURL(getActivity())
                    + "/api/v1/BannerImages/Image/" + "five_hundred_"+ bannerImage.getImageFilename() + ".jpg";



            Picasso.get()
                    .load(imagePath)
                    .into(resultView);
        }
    }





    void getDataFromViews()
    {
        if(bannerImage ==null)
        {
            if(current_mode == MODE_ADD)
            {
                bannerImage = new BannerImage();
            }
            else
            {
                return;
            }
        }


        if(!imageID.getText().toString().equals(""))
        {
            bannerImage.setBannerImageID(Integer.parseInt(imageID.getText().toString()));
        }

        if(!shopIDToOpen.getText().toString().equals(""))
        {
            bannerImage.setShopIdToOpen(Integer.parseInt(shopIDToOpen.getText().toString()));
        }


        if(!itemIDToOpen.getText().toString().equals(""))
        {
            bannerImage.setItemIDToOpen(Integer.parseInt(itemIDToOpen.getText().toString()));
        }


        if(!imageOrder.getText().toString().equals(""))
        {
            bannerImage.setSortOrder(Integer.parseInt(imageOrder.getText().toString()));
        }


        bannerImage.setShowInFrontScreen(showInFrontScreen.isChecked());
        bannerImage.setShowInItemsScreen(showInItemScreen.isChecked());
        bannerImage.setShowInShopHome(showInShopScreen.isChecked());


    }


    @Override
    public void onResume() {
        super.onResume();
        isDestroyed = false;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isDestroyed = true;
    }






    private void retrofitPUTRequest()
    {

        getDataFromViews();

        buttonUpdateItem.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);




        Call<ResponseBody> call = bannerImageService.updateBannerImage(
                PrefLogin.getAuthorizationHeader(getContext()),
                bannerImage,
                bannerImage.getBannerImageID()
        );





        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if(isDestroyed)
                {
                    return;
                }


                if(response.code()==200)
                {
                    showToastMessage("Update Successful !");
                }
                else if(response.code()== 403 || response.code() ==401)
                {
                    showToastMessage("Not Permitted !");
                }
                else
                {
                    showToastMessage("Update Failed Code : " + response.code());
                }



                buttonUpdateItem.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {


                if(isDestroyed)
                {
                    return;
                }



                showToastMessage("Update failed !");


                buttonUpdateItem.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }




    private void retrofitPOSTRequest()
    {
        getDataFromViews();

        buttonUpdateItem.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        Call<BannerImage> call = bannerImageService.createBannerImage(PrefLogin.getAuthorizationHeader(getContext()), bannerImage);

        call.enqueue(new Callback<BannerImage>() {
            @Override
            public void onResponse(Call<BannerImage> call, Response<BannerImage> response) {

                if(isDestroyed)
                {
                    return;
                }



                if(response.code()==201)
                {
                    showToastMessage("Add successful !");

                    current_mode = MODE_UPDATE;
                    updateIDFieldVisibility();
                    bannerImage = response.body();
                    bindDataToViews();


                }
                else if(response.code()== 403 || response.code() ==401)
                {
                    showToastMessage("Not Permitted !");
                }
                else
                {
                    showToastMessage("Add failed Code : " + response.code());
                }



                buttonUpdateItem.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<BannerImage> call, Throwable t) {


                if(isDestroyed)
                {
                    return;
                }


                showToastMessage("Add failed !");


                buttonUpdateItem.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);

            }
        });


    }







    /*
        Utility Methods
     */






    private void showToastMessage(String message)
    {
        UtilityFunctions.showToastMessage(getActivity(),message);
    }




    @BindView(R.id.textChangePicture)
    TextView changePicture;


    @OnClick(R.id.removePicture)
    void removeImage()
    {

        File file = new File(getContext().getCacheDir().getPath() + "/" + "SampleCropImage.jpeg");
        file.delete();

        resultView.setImageDrawable(null);

        isImageChanged = true;
        isImageRemoved = true;
    }



    public static void clearCache(Context context)
    {
        File file = new File(context.getCacheDir().getPath() + "/" + "SampleCropImage.jpeg");
        file.delete();
    }






    @OnClick({R.id.textChangePicture,R.id.uploadImage})
    void pickShopImage() {

//        ImageCropUtility.showFileChooser(()getActivity());



        // code for checking the Read External Storage Permission and granting it.
        if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PERMISSION_GRANTED) {


            /// / TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE);

            return;
        }



        ImagePicker.Companion.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(2024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1500, 1500)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start();

    }




    File imagePickedFile;
    String imageFilePath;




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {

        super.onActivityResult(requestCode, resultCode, result);



        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK

            resultView.setImageURI(result.getData());


            //You can get File object from intent
            imagePickedFile = ImagePicker.Companion.getFile(result);
            imageFilePath = ImagePicker.Companion.getFilePath(result);



            isImageChanged = true;
            isImageRemoved = false;



        } else if (resultCode == ImagePicker.RESULT_ERROR) {

            showToastMessage(ImagePicker.Companion.getError(result));

        }
        else {
            showToastMessage("Task Cancelled !");
        }

    }








    /*

    // Code for Uploading Image

     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    showToastMessage("Permission Granted !");
                    pickShopImage();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {


                    showToastMessage("Permission Denied for Read External Storage . ");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }





    public void uploadPickedImage(final boolean isModeEdit)
    {

        Log.d("applog", "onClickUploadImage");


        // code for checking the Read External Storage Permission and granting it.
        if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PERMISSION_GRANTED) {


            /// / TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE);

            return;
        }


//        File file = new File(getContext().getCacheDir().getPath() + "/" + "SampleCropImage.jpeg");


        File file;
        file = new File(imageFilePath);


        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("img", file.getName(), requestBody);


        // Marker

        RequestBody requestBodyBinary = null;

        InputStream in = null;

        try {
            in = new FileInputStream(file);

            byte[] buf;
            buf = new byte[in.available()];
            while (in.read(buf) != -1) ;

            requestBodyBinary = RequestBody.create(MediaType.parse("application/octet-stream"), buf);



        } catch (Exception e) {
            e.printStackTrace();
        }




        buttonUpdateItem.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);


        Call<Image> imageCall = bannerImageService.uploadImage(PrefLogin.getAuthorizationHeader(getContext()),
                fileToUpload);


        imageCall.enqueue(new Callback<Image>() {
            @Override
            public void onResponse(Call<Image> call, Response<Image> response) {


                if(isDestroyed)
                {
                    return;
                }



                if(response.code()==201)
                {
//                    showToastMessage("Image UPload Success !");

                    Image image = response.body();
                    // check if needed or not . If not needed then remove this line
//                    loadImage(image.getPath());


                    bannerImage.setImageFilename(image.getPath());

                }
                else if(response.code()==417)
                {
                    showToastMessage("Cant Upload Image. Image Size should not exceed 2 MB.");

                    bannerImage.setImageFilename(null);

                }
                else
                {
                    showToastMessage("Image Upload failed !");
                    bannerImage.setImageFilename(null);

                }

                if(isModeEdit)
                {
                    retrofitPUTRequest();
                }
                else
                {
                    retrofitPOSTRequest();
                }



                buttonUpdateItem.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onFailure(Call<Image> call, Throwable t) {


                if(isDestroyed)
                {
                    return;
                }



                showToastMessage("Image Upload failed !");
                bannerImage.setImageFilename(null);


                if(isModeEdit)
                {
                    retrofitPUTRequest();
                }
                else
                {
                    retrofitPOSTRequest();
                }



                buttonUpdateItem.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }



    void deleteImage(String filename)
    {
        Call<ResponseBody> call = bannerImageService.deleteImage(
                PrefLogin.getAuthorizationHeader(getContext()),
                filename);



        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                if(isDestroyed)
                {
                    return;
                }




                if(response.code()==200)
                    {
                        showToastMessage("Image Removed !");
                    }
                    else
                    {
//                        showToastMessage("Image Delete failed");
                    }


            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {


                if(isDestroyed)
                {
                    return;
                }



//                showToastMessage("Image Delete failed");
            }
        });
    }


}
