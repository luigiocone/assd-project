package it.unisannio.group8.model;

public class DisposalSampleFactory implements OneLineFactory {
    private final String separator;

    public DisposalSampleFactory(String separator) {
        this.separator = separator;
    }

    @Override
    public DisposalSample create(String line) {
        String[] params = line.split(separator);
        if (params.length < 6) {
            return null;
        }

        return new DisposalSample(
                params[0],
                params[1],
                params[2],
                Integer.parseInt(params[3]),
                Float.parseFloat(params[4]),
                Float.parseFloat(params[5])
        );
    }
}
