package edu.illinois.library.cantaloupe.operation;

import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleFilters;
import edu.illinois.library.cantaloupe.util.StringUtil;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Encapsulates an absolute or relative scale operation.</p>
 *
 * <p>Absolute instances will have a non-null width and/or height. Relative
 * instances will have a non-null percent and a null width and height.</p>
 */
public class Scale implements Operation {

    /**
     * Represents a resample algorithm.
     */
    public enum Filter {

        BELL("Bell", ResampleFilters.getBellFilter()),
        BICUBIC("Bicubic", ResampleFilters.getBiCubicFilter()),
        BOX("Box", ResampleFilters.getBoxFilter()),
        BSPLINE("B-Spline", ResampleFilters.getBSplineFilter()),
        HERMITE("Hermite", ResampleFilters.getHermiteFilter()),
        LANCZOS3("Lanczos3", ResampleFilters.getLanczos3Filter()),
        MITCHELL("Mitchell", ResampleFilters.getMitchellFilter()),
        TRIANGLE("Triangle", ResampleFilters.getTriangleFilter());

        private String name;
        private ResampleFilter resampleFilter;

        Filter(String name, ResampleFilter resampleFilter) {
            this.name = name;
            this.resampleFilter = resampleFilter;
        }

        public String getName() {
            return name;
        }

        /**
         * @return Equivalent ResampleFilter instance.
         */
        public ResampleFilter getResampleFilter() {
            return resampleFilter;
        }

    }

    public enum Mode {
        ASPECT_FIT_HEIGHT, ASPECT_FIT_WIDTH, ASPECT_FIT_INSIDE,
        NON_ASPECT_FILL, FULL
    }

    private Filter filter;
    private Integer height;
    private Mode scaleMode = Mode.FULL;
    private Float percent;
    private Integer width;

    /**
     * No-op constructor.
     */
    public Scale() {}

    public Scale(float percent) {
        setPercent(percent);
        setMode(Mode.ASPECT_FIT_INSIDE);
    }

    /**
     * @param width May be <code>null</code> if <code>mode</code> is
     *              {@link Mode#ASPECT_FIT_HEIGHT}.
     * @param height May be <code>null</code> if <code>mode</code> is
     *               {@link Mode#ASPECT_FIT_WIDTH}.
     * @param mode Scale mode.
     */
    public Scale(Integer width, Integer height, Mode mode) {
        setWidth(width);
        setHeight(height);
        setMode(mode);
    }

    /**
     * @return Resample filter to prefer. May be null.
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * @return Absolute pixel height. May be null.
     */
    public Integer getHeight() {
        return height;
    }

    public Mode getMode() {
        return scaleMode;
    }

    /**
     * @return Float from 0 to 1. May be null.
     */
    public Float getPercent() {
        return percent;
    }

    /**
     * @param fullSize
     * @return Resulting scale when the scale is applied to the given full
     *         size; or null if the scale mode is {@link Mode#NON_ASPECT_FILL}.
     */
    public Float getResultingScale(Dimension fullSize) {
        Float scale = null;
        if (this.getPercent() != null) {
            scale = this.getPercent();
        } else {
            switch (this.getMode()) {
                case FULL:
                    scale = 1f;
                    break;
                case ASPECT_FIT_HEIGHT:
                    scale = (float) (this.getHeight() /
                            (double) fullSize.height);
                    break;
                case ASPECT_FIT_WIDTH:
                    scale = (float) (this.getWidth() /
                            (double) fullSize.width);
                    break;
                case ASPECT_FIT_INSIDE:
                    scale = (float) Math.min(
                            this.getWidth() / (double) fullSize.width,
                            this.getHeight() / (double) fullSize.height);
                    break;
                case NON_ASPECT_FILL:
                    break;
            }
        }
        return scale;
    }

    /**
     * @param fullSize
     * @return Resulting dimensions when the scale is applied to the given full
     *         size.
     */
    @Override
    public Dimension getResultingSize(Dimension fullSize) {
        Dimension size = new Dimension(fullSize.width, fullSize.height);
        if (this.getPercent() != null) {
            size.width *= this.getPercent();
            size.height *= this.getPercent();
        } else {
            switch (this.getMode()) {
                case ASPECT_FIT_HEIGHT:
                    double scalePct = this.getHeight() / (double) size.height;
                    size.width = (int) Math.round(size.width * scalePct);
                    size.height = (int) Math.round(size.height * scalePct);
                    break;
                case ASPECT_FIT_WIDTH:
                    scalePct = this.getWidth() / (double) size.width;
                    size.width = (int) Math.round(size.width * scalePct);
                    size.height = (int) Math.round(size.height * scalePct);
                    break;
                case ASPECT_FIT_INSIDE:
                    scalePct = Math.min(
                            this.getWidth() / (double) size.width,
                            this.getHeight() / (double) size.height);
                    size.width = (int) Math.round(size.width * scalePct);
                    size.height = (int) Math.round(size.height * scalePct);
                    break;
                case NON_ASPECT_FILL:
                    size.width = this.getWidth();
                    size.height = this.getHeight();
                    break;
            }
        }
        return size;
    }

    /**
     * @return Absolute pixel width. May be null.
     */
    public Integer getWidth() {
        return width;
    }

    @Override
    public boolean hasEffect() {
        return (!Mode.FULL.equals(getMode())) &&
                ((getPercent() != null && Math.abs(getPercent() - 1f) > 0.000001f) ||
                        (getPercent() == null && (getHeight() != null || getWidth() != null)));
    }

    @Override
    public boolean hasEffect(Dimension fullSize, OperationList opList) {
        if (!hasEffect()) {
            return false;
        }

        Dimension cropSize = fullSize;
        for (Operation op : opList) {
            if (op instanceof Crop) {
                cropSize = op.getResultingSize(cropSize);
            }
        }

        switch (getMode()) {
            case ASPECT_FIT_WIDTH:
                return getWidth() != cropSize.width;
            case ASPECT_FIT_HEIGHT:
                return getHeight() != cropSize.height;
            default:
                if (getPercent() != null) {
                    return Math.abs(this.getPercent() - 1f) > 0.000001f;
                }
                return getWidth() != cropSize.width || getHeight() != cropSize.height;
        }
    }

    /**
     * @param comparedToSize
     * @return Whether the instance would effectively upscale the image it is
     *         applied to, i.e. whether the resulting image would have more
     *         pixels.
     */
    public boolean isUp(Dimension comparedToSize) {
        Dimension resultingSize = getResultingSize(comparedToSize);
        return resultingSize.width * resultingSize.height >
                comparedToSize.width * comparedToSize.height;
    }

    /**
     * @param filter Resample filter to prefer.
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * @param height Integer greater than 0
     * @throws IllegalArgumentException
     */
    public void setHeight(Integer height) throws IllegalArgumentException {
        if (height != null && height <= 0) {
            throw new IllegalArgumentException("Height must be a positive integer");
        }
        this.height = height;
    }

    /**
     * N.B. Invoking this method also sets the instance's mode to
     * {@link Mode#ASPECT_FIT_INSIDE}.
     *
     * @param percent Float greater than 0.
     * @throws IllegalArgumentException
     */
    public void setPercent(Float percent) throws IllegalArgumentException {
        if (percent != null && percent <= 0) {
            throw new IllegalArgumentException("Percent must be greater than zero");
        }
        this.setMode(Mode.ASPECT_FIT_INSIDE);
        this.percent = percent;
    }

    public void setMode(Mode scaleMode) {
        this.scaleMode = scaleMode;
    }

    /**
     * @param width Integer greater than 0.
     * @throws IllegalArgumentException
     */
    public void setWidth(Integer width) throws IllegalArgumentException {
        if (width != null && width <= 0) {
            throw new IllegalArgumentException("Width must be a positive integer");
        }
        this.width = width;
    }

    /**
     * @param fullSize Full size of the source image on which the operation
     *                 is being applied.
     * @return Map with <code>width</code> and <code>height</code> keys
     *         and integer values corresponding to the resulting pixel size of
     *         the operation.
     */
    @Override
    public Map<String,Object> toMap(Dimension fullSize) {
        final Dimension resultingSize = getResultingSize(fullSize);
        final Map<String,Object> map = new HashMap<>();
        map.put("class", Scale.class.getSimpleName());
        map.put("width", resultingSize.width);
        map.put("height", resultingSize.height);
        return map;
    }

    /**
     * <p>Returns a string representation of the instance, guaranteed to
     * uniquely represent the instance. The format is:</p>
     *
     * <dl>
     *     <dt>No-op</dt>
     *     <dd><code>none</code></dd>
     *     <dt>Percent</dt>
     *     <dd><code>nnn%(,filter)</code></dd>
     *     <dt>Aspect-fit-inside</dt>
     *     <dd><code>!w,h(,filter)</code></dd>
     *     <dt>Other</dt>
     *     <dd><code>w,h(,filter)</code></dd>
     * </dl>
     *
     * @return String representation of the instance.
     */
    @Override
    public String toString() {
        String str = "";
        if (!hasEffect()) {
            return "none";
        } else if (this.getPercent() != null) {
            str += StringUtil.removeTrailingZeroes(this.getPercent() * 100) + "%";
        } else {
            if (this.getMode().equals(Mode.ASPECT_FIT_INSIDE)) {
                str += "!";
            }
            if (this.getWidth() != null && this.getWidth() > 0) {
                str += this.getWidth();
            }
            str += ",";
            if (this.getHeight() != null && this.getHeight() > 0) {
                str += this.getHeight();
            }
        }
        /* TODO: fix this
        This is commented out because the filter is usually added relatively
        late in the execution by a processor's process() method. This causes
        problems with the derivative caches, which have already created a
        cached image filename/identifier based on the op list BEFORE the op
        list is aware of any filter. It writes to the file with the
        pre-filter-added name, but if the connection closes, it won't be able
        to find it later using the post-filter-added name.

        Possible solutions:
        1) Add the filter earlier in the execution (perhaps immediately once
           it is known what processor is going to be used)
        2) Make filter selection global across processors and add it in
           AbstractResource.addNonEndpointOperations() (this is how all the
           other non-endpoint operations work)
        3) Leave this as-is and don't encode the filter in the string
           representation

        if (getFilter() != null) {
            str += "," + getFilter().toString().toLowerCase();
        }
        */
        return str;
    }

}
