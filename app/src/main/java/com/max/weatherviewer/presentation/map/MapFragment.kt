package com.max.weatherviewer.presentation.map



/*
class MapFragment(parent: Kodein) : Fragment()*/
/*, KodeinAware*//*
, CoroutineScope {

    override val coroutineContext: CoroutineContext

    */
/*override val kodein: Kodein by Kodein.lazy {
        extend(parent)

        import(geocoderModule())
        import(mapModule(args<MapFragmentArgs>().preSelectedLocation))
    }

    override val coroutineContext by LifecycleAwareContext()
    private val mapComponent: MapComponent by instance("map")
    private val geoComponent: GeodecoderComponent by instance()*//*

    private val viewJobs: MutableList<Job>

    init {
        coroutineContext = Dispatchers.Main + SupervisorJob()
        viewJobs = mutableListOf()
    }

  */
/*  private val mapFragment by lazy {
        childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
    }*//*


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return MapView(requireContext(), GoogleMapOptions().zoomControlsEnabled(true).zoomControlsEnabled(true))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (view as ViewGroup).setContent {

            FloatingActionButton {

            }
        }

        */
/*viewJobs += launch {
            val map = mapFragment.googleMap()
            val marker = map.addMarker(marker(map.cameraPosition.target))
            val selections = btn_select.clicks().map { Message.Select }

            mapComponent(map.cameraChanges.mergeWith(selections.consumeAsFlow()))
                .collect { state -> render(state, map, marker) }
        }

        viewJobs += launch {
            geoComponent.changes().collect { render(it) }
        }*//*

    }

    override fun onDestroyView() {
        viewJobs.dispose()
        super.onDestroyView()
    }

    private fun render(state: GeoState) {
        when (state) {
            is Preview -> tv_manual_location.setText(state.address)
        }.safe
    }

    private fun render(state: MapState, map: GoogleMap, marker: Marker) {
        val latLng = state.location.run { LatLng(lat, lon) }

        map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder(map.cameraPosition)
                                                                    .target(latLng).zoom(state.zoom).tilt(state.tilt).bearing(state.bearing).build()))
        marker.moveAnimated(latLng,
                            resources.getInteger(android.R.integer.config_mediumAnimTime).toULong())
    }

}*/
