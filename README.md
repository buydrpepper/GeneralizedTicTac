<h1>Generalizes Tic Tac Toe (n,m,k game)</h1>
<b>Try the live demo</b>: <a href="https://buydrpepper.github.io/generalizedtictac/">https://buydrpepper.github.io/generalizedtictac/</a> </br>
Usage: Download the release and run the .jar file. Alternatively, download and run on the latest netbeans IDE<br/>
<p><b>Note:</b> There is currently a bug in the computer player logic where it underestimates lines by one turn when there are two free ends. This means that if you can get a line with (winninglength-2) and 2 free ends, you will probably win by trying to complete the line.</p>

<h2>About</h2>
<p>-This is a game I made for a school assignment a while back. Try a variety of board sizes and winning lengths.</p>
<p>-The solving algorithm is purely heuristic, so it will lose to any optimized algorithm with a high search depth. However, it will beat any naive minmax algorithm (due to search depth limitations) and will probably beat you when playing on higher board sizes.</p>
<p>You can let it go first by toggling "Playing First". It always plays near the pieces already on the board, and plays a random middle square on its first move.</p>
