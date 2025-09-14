import * as React from "preact/compat";
import { FunctionalComponent } from "preact";
import { useMemo } from "preact/hooks";

interface IdenticonProps {
  seed: string;       // Required seed (e.g., API key)
  size?: number;      // Number of blocks (width/height)
  scale?: number;     // Pixel scale for rendering
  color?: string;     // Foreground color
  bgColor?: string;   // Background color
  spotColor?: string; // Spot color
  className?: string; // Optional CSS class
}

const UiIdenticon: FunctionalComponent<IdenticonProps> = ({
  seed,
  size = 8,
  scale = 4,
  color,
  bgColor,
  spotColor,
  className = 'identicon',
}) => {
  const generateIdenticon = ({
    seed,
    size,
    scale,
    color,
    bgColor,
    spotColor,
  }: IdenticonProps) => {
    // Xorshift PRNG
    const randseed = new Array(4).fill(0); // [x, y, z, w] 32-bit values

    function seedrand(seed: string) {
      for (let i = 0; i < randseed.length; i++) {
        randseed[i] = 0;
      }
      for (let i = 0; i < seed.length; i++) {
        randseed[i % 4] = ((randseed[i % 4] << 5) - randseed[i % 4]) + seed.charCodeAt(i);
      }
    }

    function rand() {
      const t = randseed[0] ^ (randseed[0] << 11);
      randseed[0] = randseed[1];
      randseed[1] = randseed[2];
      randseed[2] = randseed[3];
      randseed[3] = randseed[3] ^ (randseed[3] >> 19) ^ t ^ (t >> 8);
      return (randseed[3] >>> 0) / ((1 << 31) >>> 0);
    }

    function createColor() {
      const h = Math.floor(rand() * 360);
      const s = (rand() * 60 + 40) + '%';
      const l = ((rand() + rand() + rand() + rand()) * 25) + '%';
      return `hsl(${h},${s},${l})`;
    }

    function createImageData(size: number) {
      const width = size;
      const height = size;
      const dataWidth = Math.ceil(width / 2);
      const mirrorWidth = width - dataWidth;
      const data: number[] = [];

      for (let y = 0; y < height; y++) {
        let row: number[] = [];
        for (let x = 0; x < dataWidth; x++) {
          row[x] = Math.floor(rand() * 2.3);
        }
        const r = row.slice(0, mirrorWidth).reverse();
        row = row.concat(r);
        data.push(...row);
      }
      return data;
    }

    // Generate colors if not provided
    seedrand(seed);
    const finalColor = color || createColor();
    const finalBgColor = bgColor || createColor();
    const finalSpotColor = spotColor || createColor();
    const imageData = createImageData(size);

    return { imageData, finalColor, finalBgColor, finalSpotColor };
  };

  // Generate identicon data
  const { imageData, finalColor, finalBgColor, finalSpotColor } = useMemo(
    () => generateIdenticon({ seed, size, scale, color, bgColor, spotColor }),
    [seed, size, scale, color, bgColor, spotColor]
  );

  // Create SVG elements
  const width = size * scale;
  const rects = imageData.map((value, i) => {
    if (value === 0) return null; // Skip background
    const row = Math.floor(i / size);
    const col = i % size;
    const fill = value === 1 ? finalColor : finalSpotColor;
    return (
      <rect
        key={`${row}-${col}`}
        x={col * scale}
        y={row * scale}
        width={scale}
        height={scale}
        fill={fill}
      />
    );
  });

  return (
    <svg
      className={className}
      width={width}
      height={width}
      viewBox={`0 0 ${width} ${width}`}
      style={{ backgroundColor: finalBgColor }}
    >
      {rects}
    </svg>
  );
};

UiIdenticon.defaultProps = {
  size: 8,
  scale: 4,
  className: "identicon",
} as Partial<IdenticonProps>;

export default UiIdenticon;