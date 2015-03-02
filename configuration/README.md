# ezbake-configuration-api

ezbake-configuration-api allows configuration values to be loaded from multiple sources depending on applications and their deployment environments.

## Usage

Most configurable objects expected `Properties`, which can be loaded from `EzConfiguration` by calling `getProperties` on an `EzConfiguration` object.

To load default configuration:

    EzConfiguration configuration = new EzConfiguration();

To load an empty configuration:

    EzConfiguration configuration = new EzConfiguration(new EzConfigurationLoader[0]);

## Maven

    <dependency>
      <groupId>ezbake</groupId>
      <artifactId>ezbake-configuration-api</artifactId>
      <version>2.0-SNAPSHOT</version>
    <dependency>
