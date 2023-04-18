
import { Cell, CodeCell, CodeCellModel, CellModel } from '@jupyterlab/cells';

// import { IOutputAreaModel } from '@jupyterlab/outputarea';
// import { IMapChange, ISharedCell, ISharedCodeCell, ISharedMarkdownCell, ISharedRawCell } from '@jupyter/ydoc';


export class LLMCell extends CodeCell {
    public constructor(options:CodeCell.IOptions) {
        super(options);
    }

    public expand(): void {
        console.log("Hello from the inside!");
    }

};


export class LLMCellModel extends CodeCellModel {

};


// export declare namespace LLMCellModel {


//    /**
//     * The options used to initialize a `CodeCellModel`.
//     */
//    interface IOptions extends Omit<CodeCellModel.IOptions, 'cell_type'> {
//        /**
//         * The factory for output area model creation.
//         */
//        contentFactory?: IContentFactory;
//    }
//    /**
//     * A factory for creating code cell model content.
//     */
//    interface IContentFactory {
//        /**
//         * Create an output area.
//         */
//        createOutputArea(options: IOutputAreaModel.IOptions): IOutputAreaModel;
//    }
//    /**
//     * The default implementation of an `IContentFactory`.
//     */
//    class ContentFactory implements IContentFactory {
//        /**
//         * Create an output area.
//         */
//        createOutputArea(options: IOutputAreaModel.IOptions): IOutputAreaModel;
//    }
//    /**
//     * The shared `ContentFactory` instance.
//     */
//    const defaultContentFactory: ContentFactory;
// }
