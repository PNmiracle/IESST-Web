import { readdir, readFile, writeFile } from "node:fs/promises";
import { extname, join } from "node:path";
import { fileURLToPath } from "node:url";

const outputDirectory = fileURLToPath(new URL("../dist/", import.meta.url));
const basePath = process.env.VITE_BASE_PATH || "/";
const textExtensions = new Set([".css", ".html", ".js"]);

if (!basePath.startsWith("/") || !basePath.endsWith("/")) {
  throw new Error("VITE_BASE_PATH must start and end with a slash");
}

async function prefixAssets(directory) {
  const entries = await readdir(directory, { withFileTypes: true });
  await Promise.all(entries.map(async (entry) => {
    const path = join(directory, entry.name);
    if (entry.isDirectory()) return prefixAssets(path);
    if (!textExtensions.has(extname(entry.name))) return;

    const source = await readFile(path, "utf8");
    const updated = source.replace(/\/images\//g, (match, offset) => {
      const existingPrefix = source.slice(Math.max(0, offset - basePath.length + 1), offset);
      return existingPrefix === basePath.slice(0, -1) ? match : `${basePath}images/`;
    });
    if (updated !== source) await writeFile(path, updated);
  }));
}

await prefixAssets(outputDirectory);
