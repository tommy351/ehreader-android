package tw.skyarrow.ehreader.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by SkyArrow on 2015/9/26.
 */
public class PhotoPageResponse {
    /*
    {
  "p": 1,
  "s": "s/1463dfbc16/618395-1",
  "n": "<div class=\"sn\"><a onclick=\"return load_image(1, '1463dfbc16')\" href=\"http://g.e-hentai.org/s/1463dfbc16/618395-1\"><img src=\"http://ehgt.org/g/f.png\" /></a><a id=\"prev\" onclick=\"return load_image(1, '1463dfbc16')\" href=\"http://g.e-hentai.org/s/1463dfbc16/618395-1\"><img src=\"http://ehgt.org/g/p.png\" /></a><div><span>1</span> / <span>20</span></div><a id=\"next\" onclick=\"return load_image(2, '8db045702f')\" href=\"http://g.e-hentai.org/s/8db045702f/618395-2\"><img src=\"http://ehgt.org/g/n.png\" /></a><a onclick=\"return load_image(20, 'f3355abf33')\" href=\"http://g.e-hentai.org/s/f3355abf33/618395-20\"><img src=\"http://ehgt.org/g/l.png\" /></a></div>",
  "i": "<div>IMG_0032.jpg :: 1200 x 1695 :: 159.20 KB</div>",
  "k": "1463dfbc16",
  "i3": "<a onclick=\"return load_image(2, '8db045702f')\" href=\"http://g.e-hentai.org/s/8db045702f/618395-2\"><img id=\"img\" src=\"http://72.174.21.70:11075/h/95d6686fa26081e921a4342deea414522604feed-163025-1200-1695-jpg/keystamp=1393677900-297404a433/IMG_0032.jpg\" style=\"width:1200px;height:1695px\" /></a>",
  "i5": "<div class=\"sb\"><a href=\"http://g.e-hentai.org/g/618395/0439fa3666/\"><img src=\"http://ehgt.org/g/b.png\" /></a></div>",
  "i6": " &nbsp; <img src=\"http://ehgt.org/g/mr.gif\" class=\"mr\" /> <a href=\"http://g.e-hentai.org/?f_shash=1463dfbc16847c9ebef92c46a90e21ca881b2a12;95d6686fa26081e921a4342deea414522604feed&amp;fs_from=IMG_0032.jpg+from+%28Kouroumu+8%29+%5BHandful%E2%98%86Happiness%21+%28Fuyuki+Nanahara%29%5D+TOUHOU+GUNMANIA+A2+%28Touhou+Project%29\">Show all galleries with this file</a>  &nbsp; <img src=\"http://ehgt.org/g/mr.gif\" class=\"mr\" /> <a href=\"#\" onclick=\"prompt('Copy the URL below.', 'http://g.e-hentai.org/r/95d6686fa26081e921a4342deea414522604feed-163025-1200-1695-jpg/forumtoken/618395-1/IMG_0032.jpg'); return false\">Generate a static forum image link</a>  &nbsp; <img src=\"http://ehgt.org/g/mr.gif\" class=\"mr\" /> <a href=\"#\" onclick=\"return nl(6049)\">Click here if the image fails loading</a> ",
  "i7": " &nbsp; <img src=\"http://ehgt.org/g/mr.gif\" class=\"mr\" /> <a href=\"http://g.e-hentai.org/fullimg.php?gid=618395&amp;page=1&amp;key=7079f01190\">Download original 4271 x 6032 1.65 MB source</a>",
  "si": 6049,
  "x": "1200",
  "y": "1695"
}
     */

    @SerializedName("p")
    private int page;

    @SerializedName("s")
    private String url;

    @SerializedName("k")
    private String token;

    @SerializedName("i3")
    private String content;

    @SerializedName("si")
    private int size;

    @SerializedName("x")
    private String width;

    @SerializedName("y")
    private String height;

    public int getHeight() {
        return Integer.valueOf(height, 10);
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getToken() {
        return token;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return Integer.valueOf(width, 10);
    }

    public String getContent() {
        return content;
    }
}
