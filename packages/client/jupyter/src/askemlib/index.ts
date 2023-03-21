import pythonCode from './python';
import juliaCode from './julia';

interface LangMap {
    [key: string]: Map
}

interface Map {
    [key: string]: string
}

export const code: LangMap = {
    python: pythonCode,
    julia: juliaCode,
};

export const getCodeBlock = (language: string, context?: string | undefined, argMap?: Map) : string => {
    const codeMap = code[language];
    const initCode = codeMap.init;
    let codeBlock = (typeof context !== 'undefined' ? codeMap[context] : "") || "";

    if (typeof argMap !== 'undefined') {
        codeBlock = codeBlock.replace(RegExp('{filename}', 'i'), argMap.filename);
    }

    let result = [initCode.trim(), codeBlock.trim()].join("\n\n");
    return result;
};
