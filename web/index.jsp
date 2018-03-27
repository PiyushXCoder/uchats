<!DOCTYPE html>
<html>
   <head>
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <meta charset="utf-8">
      <title>UChats</title>
      <link rel="stylesheet" href="style.css">
      <link rel="stylesheet" href="katex/katex.min.css">
      <link id="scheme-link" rel="stylesheet" href="schemes/atelier-dune.css">
      <script src="katex/katex.min.js"></script><script src="katex/contrib/auto-render.min.js"></script>
   </head>
   <body>
      <article class="container">
         <div id="messages" class="messages"></div>
      </article>
      <footer id="footer">
         <div class="container">
            <span class="select-wrapper" style="background-image: url(images/image.png);"><input class="fotterbutton" type="file" id="uploadimage" value="Upload Image" accept="image/*"></span>
            <span class="select-wrapper" style="background-image: url(images/file.png);"><input class="fotterbutton" type="file" id="uploadfile" value="Upload File"></span>
            <form id="chatform" class="messages"><textarea id="chatinput" type="text" autocomplete="off" autofocus></textarea></form>
         </div>
      </footer>
      <nav id="sidebar">
         <div id="sidebar-content" class="hidden">
            <p><input id="pin-sidebar" type="checkbox"><label for="pin-sidebar">Pin sidebar</label></p>
            <h4>Settings</h4>
            <p><input id="joined-left" type="checkbox" checked><label for="joined-left">Join/left notify</label></p>
            <p><input id="parse-latex" type="checkbox" checked><label for="parse-latex">Parse LaTeX</label></p>
            <p><button id="clear-messages">Clear messages</button></p>
            <h4>Color scheme</h4>
            <select id="scheme-selector"></select>
            <h4>Users online</h4>
            <p>(Click user to invite)</p>
            <ul id="users"></ul>
         </div>
      </nav>
      <script src="client.js"></script>
   </body>
</html>
