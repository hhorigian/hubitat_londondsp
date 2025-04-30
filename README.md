# hubitat_londondsp
Driver para o DSP London Soundweb - BLU-101

- Documentação do produto: https://bssaudio.com/en/products/blu-101



/* *********  Dashboard Settings  ********* */
	.dashBack { display: none; } 
	.dashName { display: none; } 

.tile {background-size: contain !important;} 

.dashboard div .header {
    position: fixed;
    bottom: -0.5em;
    right: 0.5em;
    z-index: 999;
    opacity: 1.0;
    zoom: 1.0;
    -moz-transform:scale(1.0);
}
.wrapper {
    height: 100%;
    margin-top: 0;
    padding-top: 0.5em;
}

/* *********  Tile Settings   *********  */
.tile {background-size: 50% !important;} 
.tile {background-color: #58575c;} 



/* *********  Remove Player, next, music Player Settings   *********  */

.material-icons.music-player.nextTrack
{display: none;}
.material-icons.music-player.previousTrack
{display: none;}
.trackDescription { visibility: hidden; }
.material-icons.music-player.previousTrack
{display: none;}


/* *********  Output Tile Settings   *********  */
.material-icons.music-player.unmute 
 { color: red }
.material-icons.music-player.mute 
 { color: green }



/* *********  Output Tile Settings   *********  */
#tile-20 .dimmer, #tile-19 .dimmer,  #tile-18 .dimmer, #tile-17 .dimmer, #tile-16 .dimmer, #tile-15 .dimmer, #tile-14 .dimmer, #tile-13 .dimmer
{ visibility: hidden; }

#tile-20 .material-icons.music-player.mute, #tile-19 .material-icons.music-player.mute,  #tile-18 .material-icons.music-player.mute, #tile-17 .material-icons.music-player.mute, #tile-16 .material-icons.music-player.mute, #tile-15 .material-icons.music-player.mute, #tile-14 .material-icons.music-player.mute, #tile-13 .material-icons.music-player.mute
 { font-size :40px !important; margin-top: 20px; color: green }

#tile-20 .tile-title , #tile-19 .tile-title ,  #tile-18 .tile-title , #tile-17 .tile-title , #tile-16 .tile-title , #tile-15 .tile-title , #tile-14 .tile-title , #tile-13 .tile-title 
{ 		font-size: 14px !important;
		text-align: Center !important;
}

#tile-20 .material-icons.music-player.unmute, #tile-19 .material-icons.music-player.unmute,  #tile-18 .material-icons.music-player.unmute, #tile-17 .material-icons.music-player.unmute, #tile-16 .material-icons.music-player.unmute, #tile-15 .material-icons.music-player.unmute, #tile-14 .material-icons.music-player.unmute, #tile-13 .material-icons.music-player.unmute
 { font-size :40px !important; margin-top: 20px; color: red }



