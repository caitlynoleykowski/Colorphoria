package com.oo0o0o.colorphoria.activities.loggedinactivities.main.imagefeed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import com.oo0o0o.colorphoria.model.MediaEntity
import com.oo0o0o.colorphoria.repository.ImageMetadataRepository
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class ImageFeedViewModel(val imageMetadataRepository: ImageMetadataRepository) : ViewModel() {

    private val disposables = CompositeDisposable()

    // Outputs
    val mutableMediaEntities = MutableLiveData<List<MediaEntity>>()
    val mediaEntities: LiveData<List<MediaEntity>>
        get() {
            return mutableMediaEntities
        }
    val mutableFetchComplete = MutableLiveData<Boolean>()
    val fetchComplete: LiveData<Boolean>
        get() {
            return mutableFetchComplete
        }

    // Inputs
    data class Inputs(
        val refreshes: Flowable<Unit>
    )

    fun unsubscribe() {
        disposables.clear()
    }

    fun bindUI(inputs: Inputs) {
        imageMetadataRepository.fetchFeedImages()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            mutableFetchComplete.value = true
                        },
                        {
                            // Show error
                            // Utils.showCustomToast(activity, "Unable to refresh feed")
                        }
                ).addTo(disposables)

        inputs.refreshes
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap {
                    val flowable: Flowable<Unit> = imageMetadataRepository.fetchFeedImages().toFlowable()
                    return@flatMap flowable
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            mutableFetchComplete.value = true
                        },
                        {
                            // Show error
                            // Utils.showCustomToast(activity, "Unable to refresh feed")
                        }
                ).addTo(disposables)

        // Load the media urls from the database and display the images (if in db, should be cached)
        imageMetadataRepository.getFeedImages()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mutableMediaEntities.value = it
                }.addTo(disposables)
    }
}
